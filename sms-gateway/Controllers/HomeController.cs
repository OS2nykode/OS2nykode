using System;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using SmsGateway;

public class HomeController : Controller
{
    [HttpGet]
    [Route("/")]
    public ContentResult Index()
    {
        return Content("An API to send SMS through various providers.");
    }
}