const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, ImageRun,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, PageNumber, PageBreak, LevelFormat
} = require("docx");

const PAGE_W = 11906; // A4
const PAGE_H = 16838;
const MARGIN = 1440;
const CONTENT_W = PAGE_W - 2 * MARGIN; // 9026

const border = { style: BorderStyle.SINGLE, size: 1, color: "BBBBBB" };
const borders = { top: border, bottom: border, left: border, right: border };
const cellMargins = { top: 60, bottom: 60, left: 100, right: 100 };

const BLUE = "2E5090";
const LIGHT_BLUE = "D6E4F0";

function headerCell(text, width) {
  return new TableCell({
    borders,
    width: { size: width, type: WidthType.DXA },
    shading: { fill: BLUE, type: ShadingType.CLEAR },
    margins: cellMargins,
    verticalAlign: "center",
    children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text, bold: true, font: "Arial", size: 20, color: "FFFFFF" })] })]
  });
}

function dataCell(text, width, opts = {}) {
  return new TableCell({
    borders,
    width: { size: width, type: WidthType.DXA },
    shading: opts.shading ? { fill: opts.shading, type: ShadingType.CLEAR } : undefined,
    margins: cellMargins,
    children: [new Paragraph({ alignment: opts.align || AlignmentType.CENTER, children: [new TextRun({ text, font: "Arial", size: 20, bold: opts.bold || false })] })]
  });
}

function heading1(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_1, spacing: { before: 360, after: 200 }, children: [new TextRun({ text, bold: true, font: "Arial", size: 28, color: BLUE })] });
}

function heading2(text) {
  return new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240, after: 140 }, children: [new TextRun({ text, bold: true, font: "Arial", size: 24, color: BLUE })] });
}

function para(text, opts = {}) {
  return new Paragraph({
    spacing: { after: opts.afterSpacing || 120, line: 276 },
    alignment: opts.align || AlignmentType.JUSTIFIED,
    children: [new TextRun({ text, font: "Arial", size: 22, ...opts })]
  });
}

function imgParagraph(path, w, h) {
  const data = fs.readFileSync(path);
  const ext = path.split(".").pop();
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 120 },
    children: [new ImageRun({
      type: ext,
      data,
      transformation: { width: w, height: h },
      altText: { title: path, description: path, name: path }
    })]
  });
}

function figCaption(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 200 },
    children: [new TextRun({ text, font: "Arial", size: 18, italics: true, color: "555555" })]
  });
}

// ─── Build document ──────────────────────────────────────────────────────────

const chartsDir = "C:\\Users\\ali_k\\Desktop\\AZ ödev\\charts\\";

const children = [];

// ─── Title page ──────────────────────────────────────────────────────────────
children.push(new Paragraph({ spacing: { before: 3000 }, children: [] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, children: [new TextRun({ text: "Bilgisayar Sistemleri Performans", font: "Arial", size: 36, bold: true, color: BLUE })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 400 }, children: [new TextRun({ text: "Değerlendirmesi Dersi", font: "Arial", size: 36, bold: true, color: BLUE })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: "─".repeat(40), font: "Arial", size: 24, color: "AAAAAA" })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, children: [new TextRun({ text: "Proje Ödevi", font: "Arial", size: 32, bold: true })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 600 }, children: [new TextRun({ text: "İlişkisel ve NoSQL Veritabanlarının Performans Karşılaştırması", font: "Arial", size: 28, color: "333333" })] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: "MySQL vs MongoDB", font: "Arial", size: 26, bold: true, color: BLUE })] }));
children.push(new Paragraph({ spacing: { before: 2000 }, children: [] }));
children.push(new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 100 }, children: [new TextRun({ text: "Mayıs 2026", font: "Arial", size: 22, color: "666666" })] }));

children.push(new Paragraph({ children: [new PageBreak()] }));

// ─── 1. Giriş ──────────────────────────────────────────────────────────────
children.push(heading1("1. Giriş"));
children.push(para("Günümüzde veri hacimlerinin hızla artması ve uygulama gereksinimlerinin çeşitlenmesi, veritabanı yönetim sistemlerinin performansını kritik bir faktör haline getirmiştir. Geleneksel ilişkisel veritabanları (RDBMS) yıllardır yapılandırılmış veri depolamanın temelini oluştururken, NoSQL veritabanları esnek şema yapıları ve yatay ölçeklenebilirlik avantajlarıyla öne çıkmaktadır."));
children.push(para("Bu çalışmada, en yaygın ilişkisel veritabanı olan MySQL ile belge tabanlı NoSQL veritabanı MongoDB arasında kapsamlı bir performans karşılaştırması gerçekleştirilmiştir. Farklı CRUD (Create, Read, Update, Delete) işlemleri ve karmaşık sorgular altında her iki veritabanının yanıt süresi ve iş hacmi performansları ölçülmüştür."));
children.push(para("Çalışmanın amacı, farklı iş yükü senaryolarında hangi veritabanının daha uygun olduğunu belirlemek ve yazılım geliştiricilerine veritabanı seçiminde rehberlik etmektir."));

// ─── 2. Literatür Özeti ─────────────────────────────────────────────────
children.push(heading1("2. Literatür Özeti"));
children.push(heading2("2.1 İlişkisel Veritabanları (SQL)"));
children.push(para("İlişkisel veritabanları, verileri tablolar halinde organize eden ve SQL (Structured Query Language) dilini kullanan sistemlerdir. ACID (Atomicity, Consistency, Isolation, Durability) özelliklerini garanti ederek veri bütünlüğünü sağlarlar. MySQL, Oracle Corporation tarafından geliştirilen açık kaynaklı bir RDBMS olup, dünyanın en popüler veritabanlarından biridir. InnoDB depolama motoru ile B-tree indeksleme, yabancı anahtar kısıtlamaları ve işlem desteği sunmaktadır."));

children.push(heading2("2.2 NoSQL Veritabanları"));
children.push(para("NoSQL veritabanları, ilişkisel modelin dışında veri depolama yaklaşımları sunan sistemlerdir. Belge tabanlı, anahtar-değer, sütun ailesi ve çizge tabanlı olmak üzere dört ana kategoride incelenirler. MongoDB, JSON benzeri BSON belgeleri kullanan belge tabanlı bir NoSQL veritabanıdır. Şemasız yapısı sayesinde esnek veri modelleme imkânı sunar."));

children.push(heading2("2.3 İlgili Çalışmalar"));
children.push(para("Li ve ark. (2013), MongoDB ve MySQL performansını karşılaştırarak MongoDB’nin yazma işlemlerinde daha hızlı olduğunu göstermiştir. Parker ve ark. (2013), büyük veri ortamlarında NoSQL’in ölçeklenebilirlik avantajını vurgulamıştır. Cattell (2011), farklı veritabanı türlerinin performans karakteristiklerini kapsamlı bir şekilde analiz etmiştir."));

// ─── 3. Metodoloji ────────────────────────────────────────────────────────
children.push(heading1("3. Metodoloji"));
children.push(para("Bu çalışmada, Python programlama dili kullanılarak MySQL ve MongoDB üzerinde performans testleri gerçekleştirilmiştir. Veritabanı bağlantıları için mysql-connector-python ve pymongo kütüphaneleri kullanılmıştır."));
children.push(para("Test verileri, Faker kütüphanesi ile sentetik olarak üretilmiş bir e-ticaret veri seti üzerinden oluşturulmuştur. Veri seti üç tablodan oluşmaktadır: 10.000 müşteri, 5.000 ürün ve 1.000.000 sipariş kaydı. Bu yapı, gerçek dünya e-ticaret senaryolarını yansıtmaktadır."));
children.push(para("Her test senaryosu, her iki veritabanı için aynı veriler ve koşullar altında çalıştırılmıştır. Süreler Python’un time.perf_counter() fonksiyonu ile yüksek hassasiyetle ölçülmüştür."));

// ─── 4. Deney Ortamı ──────────────────────────────────────────────────────
children.push(heading1("4. Deney Ortamı"));

const c4 = [3000, 6026];
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: c4,
  rows: [
    new TableRow({ children: [headerCell("Bileşen", c4[0]), headerCell("Detay", c4[1])] }),
    new TableRow({ children: [dataCell("İşletim Sistemi", c4[0]), dataCell("Windows 11 Pro", c4[1])] }),
    new TableRow({ children: [dataCell("Python Sürümü", c4[0]), dataCell("3.13.12", c4[1])] }),
    new TableRow({ children: [dataCell("MySQL Sürümü", c4[0]), dataCell("8.4.9 (InnoDB)", c4[1])] }),
    new TableRow({ children: [dataCell("MongoDB Sürümü", c4[0]), dataCell("8.3.2", c4[1])] }),
    new TableRow({ children: [dataCell("MySQL Python Kütüphanesi", c4[0]), dataCell("mysql-connector-python 9.7.0", c4[1])] }),
    new TableRow({ children: [dataCell("MongoDB Python Kütüphanesi", c4[0]), dataCell("pymongo 4.17.0", c4[1])] }),
    new TableRow({ children: [dataCell("Veri Üretimi", c4[0]), dataCell("Faker 40.18.0", c4[1])] }),
    new TableRow({ children: [dataCell("Grafik", c4[0]), dataCell("matplotlib 3.10.9", c4[1])] }),
  ]
}));
children.push(figCaption("Tablo 1: Deney ortamı yazılım bileşenleri"));

// ─── 5. Deney Senaryoları ──────────────────────────────────────────────
children.push(heading1("5. Deney Senaryoları"));

children.push(heading2("5.1 INSERT (Yazma) Testi"));
children.push(para("Her iki veritabanına 10.000, 100.000 ve 1.000.000 sipariş kaydı toplu olarak eklenmiş ve süreleri ölçülmüştür. MySQL tarafında executemany(), MongoDB tarafında insert_many() metotları 5.000’lik gruplar halinde kullanılmıştır."));

children.push(heading2("5.2 SELECT (Okuma) Testi"));
children.push(para("Tek kayıt sorgulamasında, rastgele seçilen 10 order_id ile sorgulama yapılmış ve ortalama süre hesaplanmıştır. Aralık sorgusunda ise yaşı 25-35 arasında olan müşterilerin siparişleri getirilmiştir. MySQL tarafında JOIN, MongoDB tarafında $in operatörü kullanılmıştır."));

children.push(heading2("5.3 UPDATE (Güncelleme) Testi"));
children.push(para("1.000, 10.000 ve 100.000 sipariş kaydının status alanı güncellenmiştir. MySQL’de UPDATE ... SET, MongoDB’de update_many() kullanılmıştır."));

children.push(heading2("5.4 DELETE (Silme) Testi"));
children.push(para("10.000, 100.000 ve 300.000 sipariş kaydı silinmiştir. MySQL’de DELETE FROM, MongoDB’de delete_many() kullanılmıştır."));

children.push(heading2("5.5 Karmaşık Sorgu Testi"));
children.push(para("Ülke ve ürün kategorisine göre sipariş sayısı, toplam gelir ve ortalama sipariş değeri hesaplanmıştır. MySQL tarafında üç tablo üzerinden JOIN ve GROUP BY, MongoDB tarafında $lookup, $unwind, $group ve $sort aşamalarından oluşan aggregation pipeline kullanılmıştır."));

// ─── 6. Performans Metrikleri ─────────────────────────────────────────────
children.push(heading1("6. Performans Metrikleri"));
children.push(para("Bu çalışmada iki temel performans metriği kullanılmıştır:"));
children.push(para("Yanıt Süresi (Response Time): Bir işlemin başlangıcından tamamlanmasına kadar geçen süre (saniye cinsinden). Python’un time.perf_counter() fonksiyonu ile yüksek hassasiyetle ölçülmüştür.", { bold: false }));
children.push(para("İş Hacmi (Throughput): Birim zamanda işlenen kayıt sayısı (kayıt/saniye). INSERT testlerinde toplam kayıt sayısının geçen süreye bölünmesiyle hesaplanmıştır."));

// ─── 7. Sonuçlar ──────────────────────────────────────────────────────────
children.push(new Paragraph({ children: [new PageBreak()] }));
children.push(heading1("7. Sonuçlar"));

// INSERT table
children.push(heading2("7.1 INSERT Sonuçları"));
const ci = [2256, 2256, 2257, 2257];
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: ci,
  rows: [
    new TableRow({ children: [headerCell("Kayıt Sayısı", ci[0]), headerCell("MySQL (s)", ci[1]), headerCell("MongoDB (s)", ci[2]), headerCell("Fark", ci[3])] }),
    new TableRow({ children: [dataCell("10.000", ci[0]), dataCell("0,556", ci[1]), dataCell("0,441", ci[2]), dataCell("MongoDB %20 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("100.000", ci[0]), dataCell("4,875", ci[1]), dataCell("3,241", ci[2]), dataCell("MongoDB %33 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("1.000.000", ci[0]), dataCell("57,495", ci[1]), dataCell("37,549", ci[2]), dataCell("MongoDB %35 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
  ]
}));
children.push(figCaption("Tablo 2: INSERT performans sonuçları"));

children.push(imgParagraph(chartsDir + "insert_benchmark.png", 520, 312));
children.push(figCaption("Şekil 1: INSERT performans karşılaştırması"));

// Throughput table
children.push(heading2("7.2 Throughput Sonuçları"));
const ct = [2256, 2256, 2257, 2257];
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: ct,
  rows: [
    new TableRow({ children: [headerCell("Kayıt Sayısı", ct[0]), headerCell("MySQL (kayıt/s)", ct[1]), headerCell("MongoDB (kayıt/s)", ct[2]), headerCell("Fark", ct[3])] }),
    new TableRow({ children: [dataCell("10.000", ct[0]), dataCell("17.997", ct[1]), dataCell("22.669", ct[2]), dataCell("+%26", ct[3])] }),
    new TableRow({ children: [dataCell("100.000", ct[0]), dataCell("20.514", ct[1]), dataCell("30.859", ct[2]), dataCell("+%50", ct[3])] }),
    new TableRow({ children: [dataCell("1.000.000", ct[0]), dataCell("17.393", ct[1]), dataCell("26.632", ct[2]), dataCell("+%53", ct[3])] }),
  ]
}));
children.push(figCaption("Tablo 3: INSERT iş hacmi (throughput) sonuçları"));

// SELECT results
children.push(heading2("7.3 SELECT Sonuçları"));
const cs = [2256, 2256, 2257, 2257];
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: cs,
  rows: [
    new TableRow({ children: [headerCell("Sorgu Türü", cs[0]), headerCell("MySQL", cs[1]), headerCell("MongoDB", cs[2]), headerCell("Fark", cs[3])] }),
    new TableRow({ children: [dataCell("Tek Kayıt (ort.)", cs[0]), dataCell("12,09 ms", cs[1]), dataCell("15,93 ms", cs[2]), dataCell("MySQL %24 hızlı", cs[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("Aralık Sorgusu", cs[0]), dataCell("2,302 s", cs[1]), dataCell("2,430 s", cs[2]), dataCell("MySQL %5 hızlı", cs[3], { shading: LIGHT_BLUE })] }),
  ]
}));
children.push(figCaption("Tablo 4: SELECT performans sonuçları (189.251 kayıt)"));

children.push(imgParagraph(chartsDir + "select_benchmark.png", 560, 240));
children.push(figCaption("Şekil 2: SELECT performans karşılaştırması"));

// UPDATE results
children.push(heading2("7.4 UPDATE Sonuçları"));
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: ci,
  rows: [
    new TableRow({ children: [headerCell("Kayıt Sayısı", ci[0]), headerCell("MySQL (s)", ci[1]), headerCell("MongoDB (s)", ci[2]), headerCell("Fark", ci[3])] }),
    new TableRow({ children: [dataCell("1.000", ci[0]), dataCell("0,034", ci[1]), dataCell("0,059", ci[2]), dataCell("MySQL %42 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("10.000", ci[0]), dataCell("0,110", ci[1]), dataCell("0,214", ci[2]), dataCell("MySQL %49 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("100.000", ci[0]), dataCell("1,054", ci[1]), dataCell("2,091", ci[2]), dataCell("MySQL %50 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
  ]
}));
children.push(figCaption("Tablo 5: UPDATE performans sonuçları"));

children.push(imgParagraph(chartsDir + "update_benchmark.png", 520, 312));
children.push(figCaption("Şekil 3: UPDATE performans karşılaştırması"));

// DELETE results
children.push(heading2("7.5 DELETE Sonuçları"));
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: ci,
  rows: [
    new TableRow({ children: [headerCell("Kayıt Sayısı", ci[0]), headerCell("MySQL (s)", ci[1]), headerCell("MongoDB (s)", ci[2]), headerCell("Fark", ci[3])] }),
    new TableRow({ children: [dataCell("10.000", ci[0]), dataCell("0,441", ci[1]), dataCell("0,636", ci[2]), dataCell("MySQL %31 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("100.000", ci[0]), dataCell("3,130", ci[1]), dataCell("5,746", ci[2]), dataCell("MySQL %45 hızlı", ci[3], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("300.000", ci[0]), dataCell("10,191", ci[1]), dataCell("10,046", ci[2]), dataCell("Benzer performans", ci[3])] }),
  ]
}));
children.push(figCaption("Tablo 6: DELETE performans sonuçları"));

children.push(imgParagraph(chartsDir + "delete_benchmark.png", 520, 312));
children.push(figCaption("Şekil 4: DELETE performans karşılaştırması"));

// Complex query results
children.push(heading2("7.6 Karmaşık Sorgu Sonuçları"));
const cc = [3009, 3009, 3008];
children.push(new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: cc,
  rows: [
    new TableRow({ children: [headerCell("Veritabanı", cc[0]), headerCell("Yöntem", cc[1]), headerCell("Süre (s)", cc[2])] }),
    new TableRow({ children: [dataCell("MySQL", cc[0]), dataCell("JOIN + GROUP BY", cc[1]), dataCell("2,343", cc[2], { shading: LIGHT_BLUE })] }),
    new TableRow({ children: [dataCell("MongoDB", cc[0]), dataCell("Aggregation Pipeline", cc[1]), dataCell("137,658", cc[2])] }),
  ]
}));
children.push(figCaption("Tablo 7: Karmaşık sorgu sonuçları (MySQL 58,7x daha hızlı)"));

children.push(imgParagraph(chartsDir + "complex_benchmark.png", 440, 330));
children.push(figCaption("Şekil 5: Karmaşık sorgu karşılaştırması"));

// Overall chart
children.push(imgParagraph(chartsDir + "overall_comparison.png", 560, 326));
children.push(figCaption("Şekil 6: Genel performans karşılaştırması"));

// ─── 8. Tartışma ────────────────────────────────────────────────────────
children.push(new Paragraph({ children: [new PageBreak()] }));
children.push(heading1("8. Tartışma"));
children.push(para("Elde edilen sonuçlar, MySQL ve MongoDB’nin farklı iş yüklerinde farklı performans karakteristikleri sergilediğini ortaya koymaktadır."));

children.push(heading2("8.1 Yazma Performansı"));
children.push(para("MongoDB, tüm INSERT testlerinde MySQL’den %20-35 oranında daha hızlı performans göstermiştir. Bu farkın temel nedenleri arasında MongoDB’nin şemalı yapısının olmaması, yabancı anahtar kısıtlama kontrolü yapmaması ve BSON formatının verimli seri hale getirme (serialization) sağlaması sayılabilir. MongoDB’nin throughput değerleri de tutarlı biçimde yüksek olup, 1M kayıtta 26.632 kayıt/s’ye ulaşmıştır."));

children.push(heading2("8.2 Okuma Performansı"));
children.push(para("Tek kayıt sorgularında MySQL, MongoDB’ye göre yaklaşık %24 daha hızlıdır (12,09 ms vs 15,93 ms). Bu avantaj, MySQL’in optimize edilmiş B-tree indeks yapısı ve sorgu planlayıcısından kaynaklanmaktadır. Aralık sorgularında ise fark daha az belirgindir (%5), çünkü her iki sistem de indeksleri etkin kullanmıştır."));

children.push(heading2("8.3 Güncelleme ve Silme Performansı"));
children.push(para("MySQL, UPDATE işlemlerinde tutarlı biçimde yaklaşık %42-50 daha hızlı performans göstermiştir. Benzer şekilde DELETE işlemlerinde de MySQL daha hızlıdır, ancak 300.000 kayıt seviyesinde fark neredeyse kapanmıştır (10,191 s vs 10,046 s). Bu durum, büyük hacimli silme işlemlerinde MongoDB’nin belge tabanlı yapısının avantaj sağladığını göstermektedir."));

children.push(heading2("8.4 Karmaşık Sorgu Performansı"));
children.push(para("En çarpıcı fark karmaşık sorgu testinde ortaya çıkmıştır. MySQL, üç tablo üzerinden JOIN ve GROUP BY işlemini 2,343 saniyede tamamlarken, MongoDB’nin aggregation pipelineı 137,658 saniye sürmüştür. Bu 58,7 katlık fark, MongoDB’nin doğası gereği ilişkisel birleştirme (JOIN) işlemleri için tasarlanmamış olmasından kaynaklanmaktadır. MongoDB’deki $lookup operatörü, her belge için ayrı koleksiyonlara erişim gerektirdiğinden yüksek I/O maliyetine neden olmaktadır."));

// ─── 9. Sonuç ve Öneriler ─────────────────────────────────────────────
children.push(heading1("9. Sonuç ve Öneriler"));
children.push(para("Bu çalışmada MySQL ve MongoDB veritabanlarının farklı iş yükleri altındaki performansları kapsamlı bir şekilde karşılaştırılmıştır. Elde edilen sonuçlara göre:"));
children.push(para("MongoDB, yazma ağırlıklı iş yüklerinde (%20-35 daha hızlı INSERT) ve esnek şema gerektiren uygulamalarda tercih edilmelidir. Log toplama sistemleri, içerik yönetim sistemleri ve IoT veri depoları gibi senaryolarda MongoDB avantajlıdır."));
children.push(para("MySQL, okuma ağırlıklı iş yüklerinde, karmaşık ilişkisel sorgularda ve veri bütünlüğünün kritik olduğu uygulamalarda belirgin üstünlük sağlamaktadır. Finansal sistemler, ERP uygulamaları ve raporlama sistemleri için MySQL daha uygundur."));
children.push(para("Sonuç olarak, evrensel bir “en iyi veritabanı” yoktur. Veritabanı seçimi, uygulamanın gereksinimlerine, veri yapısına ve beklenen iş yüküne göre yapılmalıdır. Karma iş yüklerinde her iki teknolojinin bir arada (polyglot persistence) kullanılması da değerlendirilmelidir."));

// ─── 10. Kaynakça ──────────────────────────────────────────────────────
children.push(heading1("10. Kaynakça"));
const refs = [
  "Cattell, R. (2011). Scalable SQL and NoSQL Data Stores. ACM SIGMOD Record, 39(4), 12-27.",
  "Li, Y., & Manoharan, S. (2013). A Performance Comparison of SQL and NoSQL Databases. IEEE Pacific Rim Conference on Communications, Computers and Signal Processing.",
  "Parker, Z., Poe, S., & Vrbsky, S. V. (2013). Comparing NoSQL MongoDB to an SQL DB. Proceedings of the 51st ACM Southeast Conference.",
  "Abramova, V., & Bernardino, J. (2013). NoSQL Databases: MongoDB vs Cassandra. Proceedings of the International C* Conference on Computer Science and Software Engineering.",
  "Nayak, A., Poriya, A., & Poojary, D. (2013). Type of NOSQL Databases and its Comparison with Relational Databases. International Journal of Applied Information Systems, 5(4), 16-19.",
  "MongoDB Inc. (2026). MongoDB Documentation. https://docs.mongodb.com/",
  "Oracle Corporation. (2026). MySQL 8.4 Reference Manual. https://dev.mysql.com/doc/refman/8.4/en/",
  "Han, J., Haihong, E., Le, G., & Du, J. (2011). Survey on NoSQL Database. 6th International Conference on Pervasive Computing and Applications, 363-366.",
];
refs.forEach((ref, i) => {
  children.push(new Paragraph({
    spacing: { after: 80 },
    indent: { left: 500, hanging: 500 },
    children: [new TextRun({ text: `[${i + 1}] ${ref}`, font: "Arial", size: 20 })]
  }));
});

// ─── Assemble ─────────────────────────────────────────────────────────────
const doc = new Document({
  styles: {
    default: { document: { run: { font: "Arial", size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, font: "Arial", color: BLUE },
        paragraph: { spacing: { before: 360, after: 200 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: "Arial", color: BLUE },
        paragraph: { spacing: { before: 240, after: 140 }, outlineLevel: 1 } },
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: PAGE_W, height: PAGE_H },
        margin: { top: MARGIN, right: MARGIN, bottom: MARGIN, left: MARGIN }
      }
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.RIGHT,
          children: [new TextRun({ text: "MySQL vs MongoDB Performans Karşılaştırması", font: "Arial", size: 16, color: "999999", italics: true })]
        })]
      })
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: "Sayfa ", font: "Arial", size: 16, color: "999999" }), new TextRun({ children: [PageNumber.CURRENT], font: "Arial", size: 16, color: "999999" })]
        })]
      })
    },
    children
  }]
});

const outPath = "C:\\Users\\ali_k\\Desktop\\AZ ödev\\rapor.docx";
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(outPath, buffer);
  console.log("Report saved to: " + outPath);
}).catch(err => console.error("Error:", err));
