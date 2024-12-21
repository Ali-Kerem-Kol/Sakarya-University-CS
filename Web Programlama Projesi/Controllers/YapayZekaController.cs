using Google.Cloud.Vision.V1;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using Newtonsoft.Json;
using OpenAI;
using System.Text;
using Web_Programlama_Projesi.Response;


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

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar
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

            // Şimdi OpenAI API'sini kullanarak uygun saç modelini önerelim
            var hairStyleRecommendation = await GetGPTResponse(analysisResult);

            // Saç modeli önerisini kullanıcıya gösterelim
            ViewData["HairStyleRecommendation"] = hairStyleRecommendation;

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

        // OpenAI API'si ile saç modeli önerisi alıyoruz
        [HttpPost]
        private async Task<IActionResult> GetGPTResponse(string query)
        {
            // Get the OpenAPI Key from AppSettings.json
            var openAPIKey = _configuration1["OpenAI:ApiKey"];

            // Set up the HttpClient with OpenApi Key
            _httpClient.DefaultRequestHeaders.Add("Authorization", $"Bearer {openAPIKey}");

            // Define the request payload
            var payload = new
            {
                model = "gpt-3.5-turbo", // Alternatif model
                messages = new object[]
                {
                new { role = "user", content = "Bu özelliklere göre saç modeli öner: " + query }
                },
                temperature = 0,
                max_tokens = 256
            };

            string jsonPayload = JsonConvert.SerializeObject(payload);
            HttpContent httpContent = new StringContent(jsonPayload, Encoding.UTF8, "application/json");

            // Send the request
            var responseMessage = await _httpClient.PostAsync("https://api.openai.com/v1/chat/completions", httpContent);

            // API yanıtını al
            var responseMessageJson = await responseMessage.Content.ReadAsStringAsync();

            // Yanıtı log'a yaz
            _logger.LogInformation("API Yanıtı: " + responseMessageJson);

            // Yanıtı çözümle
            var response = JsonConvert.DeserializeObject<OpenAIResponse>(responseMessageJson);

            if (response?.Choices != null && response.Choices.Any() && response.Choices[0]?.Message != null)
            {
                ViewBag.Result = response.Choices[0].Message.Content;
            }
            else
            {
                ViewBag.Result = "Saç modeli önerisi alınamadı.";
            }

            return View("Index");
        }


    }
}
