using System;
using System.IO;
using System.Linq;
using System.Xml.Serialization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using OmFamilieCPRService;
using Schemas;

namespace OmFamilieCPRIntegration.Controllers
{
    [ApiController]
    [Produces("application/json")]
    [Route("api/[controller]")]
    public class CPRLookupController : ControllerBase
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private IConfiguration Configuration;

        public CPRLookupController(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        [HttpGet]
        public ActionResult Get(string cpr)
        {
            int GetBirthdayYear(rootGctpSystemServiceCprDataRolleTableRow m) => Int32.Parse(GetAttr(m, "PNR_FOEDDATO").Substring(4, 2));
            int GetCurrentYear() => Int32.Parse(DateTime.Now.ToString("yy"));
            int GetYearDifference(rootGctpSystemServiceCprDataRolleTableRow m) => GetCurrentYear() - GetBirthdayYear(m);

            if (!Validate(cpr))
            {
                return BadRequest("Invalid CPR!");
            }

            log.Info("Attempting familie lookup on: " + Hide(cpr));

            GCTPLookupRequestType request = new GCTPLookupRequestType();
            request.InvocationContext = GetInvocationContext();
            request.gctpMessage = $"<Gctp v=\"1.0\"><System r=\"CprSoeg\"><Service r=\"Familie\"><CprServiceHeader r=\"Familie\"><Key><Field r=\"PNR\" v=\"{cpr}\"/></Key></CprServiceHeader></Service></System></Gctp>";

            CprLookupServicePortTypeClient lookupService = new CprLookupServicePortTypeClient(Configuration["OmFamilieService:serviceUrl"], Configuration["OmFamilieService:certPath"], Configuration["OmFamilieService:certPassword"]);
            callGctpServiceResponse response = lookupService.callGctpServiceAsync(request).Result;

            XmlSerializer serializer = new XmlSerializer(typeof(root));
            StringReader rdr = new StringReader(response.callGCTPCheckServiceResponse.result);
            root gctpResponse = (root)serializer.Deserialize(rdr);
            rootGctpSystemServiceCprDataRolleTableRow[] members = gctpResponse.Gctp.System.Service.CprData.Rolle.Table.Row;

            // Filter only underaged kids
            members = members.Where(m => GetYearDifference(m) > 0 && GetYearDifference(m) < 18).ToArray();

            var result = members.Where(m => GetAttr(m, "FAMMRK").Equals("Barn")).Select(m => new {name = GetAttr(m, "ADRNVN"), cpr = GetAttr(m, "PNR_FOEDDATO")}).ToArray();

            return Ok(result);
        }

        private string GetAttr(rootGctpSystemServiceCprDataRolleTableRow member, string field) => member.Field.Where(f => f.r == field).Select(f => f.v).FirstOrDefault();

        private InvocationContextType GetInvocationContext()
        {
            InvocationContextType invocationContext = new InvocationContextType();

            invocationContext.ServiceUUID = "2419e94b-1760-4fc5-935c-2419ac956e79";
            invocationContext.ServiceAgreementUUID = Configuration["ServiceAgreement:uuid"];
            invocationContext.UserUUID = Configuration["ServiceAgreement:userUuid"];
            invocationContext.UserSystemUUID = Configuration["ServiceAgreement:systemUuid"];

            return invocationContext;
        }

        private string Hide(string cpr)
        {
            return cpr.Substring(0, 6) + "-XXXX";
        }

        private bool Validate(string cpr)
        {
            // 10 chars exactly
            if (cpr == null || cpr.Length != 10)
            {
                return false;
            }

            // only numbers
            foreach (char c in cpr)
            {
                if (c < '0' || c > '9')
                {
                    return false;
                }
            }

            // first 2 should be 1-31
            int days = Int32.Parse(cpr.Substring(0, 2));
            if (days < 1 || days > 31)
            {
                return false;
            }

            // second 2 should be 1-12
            int months = Int32.Parse(cpr.Substring(2, 2));
            if (months < 1 || months > 12)
            {
                return false;
            }

            return true;
        }
    }
}
