using Google.Cloud.Vision.V1;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using Newtonsoft.Json;
using OpenAI;
using System.Net.Http.Headers;
using System.Text;
using Web_Programlama_Projesi.Security;


namespace Web_Programlama_Projesi.Controllers
{
    public class YapayZekaController : Controller
    {
        private readonly string _googleVisionApiKey;
        private readonly ILogger _logger;
        private readonly IConfiguration _configuration1;
        private readonly HttpClient _httpClient;

        public YapayZekaController(IConfiguration configuration, ILogger<YapayZekaController> logger, IConfiguration configuration1, HttpClient httpClient)
        {
            _logger = logger;
            _configuration1 = configuration1;
            _httpClient = httpClient;

            // Google Vision API Key'i appsettings.json'dan alıyoruz
            _googleVisionApiKey = configuration["GoogleVisionApiKey"];

        }

        private void SetUserInfoToViewData()
        {
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null;
        }

        // Ana sayfa
        public IActionResult Index()
        {
            SetUserInfoToViewData();

            return View();
        }

        // Fotoğraf Yükleme ve İşleme
        [HttpPost]
        public async Task<IActionResult> Index(IFormFile photo)
        {

            if (photo == null || photo.Length == 0)
            {
                ViewData["Message"] = "Lütfen geçerli bir fotoğraf yükleyin.";
                return View();
            }

            // Fotoğrafı geçici olarak kaydedelim
            var filePath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", photo.FileName);

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await photo.CopyToAsync(stream);
            }

            // Fotoğrafı başarıyla kaydettik
            ViewData["Message"] = $"Fotoğraf başarıyla yüklendi: {photo.FileName}";

            // Fotoğrafı işlemek için Google Vision API çağrısı yapalım
            var analysisResult = await AnalyzeImage(filePath);

            // İşlenen sonuçları kullanıcıya gösterelim
            ViewData["Message"] += $"<br/>Analiz Sonucu: {analysisResult}";


            ViewData["Result"] = analysisResult + ",Bu Özelliklere sahip bir yüz için nasıl bir saç modeli önerirsin ?";

            return View();
        }

        // Fotoğrafı analiz etme işlemi (Google Vision API)
        private async Task<string> AnalyzeImage(string imagePath)
        {
            try
            {
                // Görseli bir MemoryStream'e yüklüyoruz
                var image = Image.FromFile(imagePath);

                // Vision API istemcisini oluşturuyoruz
                var client = ImageAnnotatorClient.Create();
                var response = await client.DetectLabelsAsync(image);

                // Görseldeki etiketleri analiz ediyoruz
                var labels = string.Join(", ", response.Select(label => label.Description));

                return labels; // Etiketleri döndürüyoruz
            }
            catch (Exception ex)
            {
                return $"Hata oluştu: {ex.Message}";
            }
        }




    }
}
