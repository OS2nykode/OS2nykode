namespace SmsGateway
{
    public interface ISMSGateway
    {
        bool SendSMS(Message message);
    }
}