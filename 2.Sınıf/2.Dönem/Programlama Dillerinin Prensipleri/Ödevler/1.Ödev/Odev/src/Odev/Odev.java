/**
*
* @author Ali Kerem Kol-ali.kol@ogr.sakarya.edu.tr
* @since 01.04.2024
* <p>
* Source code
* </p>
*/
package Odev;
	
import java.io.File;
	import java.io.IOException;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Scanner;
	
	import java.io.*;
	import java.util.*;
	
	public class Odev 
	{
	
	    public static void main(String[] args) 
	    {
	    	
	    	// URL alma
	    	Scanner scanner = new Scanner(System.in);
	
	        System.out.print("GitHub deposu URL'sini girin: ");
	        String githubURL = scanner.nextLine();
	
	        scanner.close();
	        
	        
	        // Depoyu klonlama işlemi
	        try 
	        {
	            cloneRepository(githubURL);
	        } 
	        catch (IOException e) 
	        {
	            System.err.println("Depo klonlama sırasında bir hata oluştu: " + e.getMessage());
	        }
	    	
	    	
	        // .java dosyalarını bulma
	        String clonedRepoPath = getClonedRepoPath(githubURL);
	        if (clonedRepoPath != null) 
	        {
	            List<File> javaFiles = findJavaFiles(new File(clonedRepoPath));
	            System.out.println("Klonlanan depo içindeki sınıf dosyaları:");
	            for (File javaFile : javaFiles) 
	            {
	                if (containsClass(javaFile)) 
	                {
	                    System.out.println(javaFile.getName());
	                    analyzeJavaFile(javaFile);
	                }
	            }
	        }
	        
	        
	        // Depo klasörünü sil
	        String[] parts = githubURL.split("/");
	        String repoName = parts[parts.length - 1].replace(".git", "");
	        File clonedRepoDir = new File(System.getProperty("user.dir"), repoName);
	        deleteDirectory(clonedRepoDir);
	        
	        
	    }
	    
	    // Klonlama
	    private static void cloneRepository(String githubURL) throws IOException 
	    {
	        ProcessBuilder processBuilder = new ProcessBuilder();
	        processBuilder.command("git", "clone", githubURL);
	
	        Process process = processBuilder.start();
	
	        // Klonlama işlemini takip etmek için çıktıyı oku
	        Scanner scanner = new Scanner(process.getInputStream());
	        while (scanner.hasNextLine()) 
	        {
	            System.out.println(scanner.nextLine());
	        }
	        scanner.close();
	
	        // Klonlama işlemi tamamlandıysa başarı mesajını yazdır
	        System.out.println("Depo klonlama işlemi başarıyla tamamlandı.");
	    }
	    
	    // .java dosyalarını bulma
	    private static String getClonedRepoPath(String githubURL) 
	    {
	        String[] parts = githubURL.split("/");
	        String repoName = parts[parts.length - 1].replace(".git", "");
	        File clonedRepoDir = new File(System.getProperty("user.dir"), repoName);
	        if (clonedRepoDir.exists()) 
	        {
	            return clonedRepoDir.getAbsolutePath();
	        }
	        return null;
	    }
	
	    private static List<File> findJavaFiles(File directory) 
	    {
	        List<File> javaFiles = new ArrayList<>();
	        if (directory.isDirectory()) 
	        {
	            for (File file : directory.listFiles()) 
	            {
	                if (file.isDirectory()) 
	                {
	                    javaFiles.addAll(findJavaFiles(file));
	                } 
	                else if (file.isFile() && file.getName().endsWith(".java")) {
	                    javaFiles.add(file);
	                }
	            }
	        }
	        return javaFiles;
	    }
	    
	    // Sınıf içeren dosyaları ayıklama
	    private static boolean containsClass(File javaFile) 
	    {
	        try (Scanner scanner = new Scanner(javaFile)) 
	        {
	            while (scanner.hasNextLine()) 
	            {
	                String line = scanner.nextLine();
	                if (line.contains("class")) 
	                {
	                    return true;
	                }
	            }
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	        return false;
	    }
	    
	    // Java dosyasını analiz etme
	    private static void analyzeJavaFile(File javaFile) 
	    {
	        int javadocSatir = 0;
	        int yorumSatir = 0;
	        int kodSatir = 0;
	        int toplamSatir = 0;
	        int fonksiyonSayisi = 0;
	        int bosSatir = 0;
	        boolean inJavadoc = false;
	        boolean inCommentBlock = false;

	        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) 
	        {
	            String line;
	            while ((line = reader.readLine()) != null) 
	            {
	                line = line.trim();

	                // Javadoc ve yorum satırlarını kontrol et
	                if (line.startsWith("/**")) 
	                {
	                    inJavadoc = true;
	                } 
	                else if (line.startsWith("/*")) 
	                {
	                	inCommentBlock = true;
	                } 
	                else if (line.startsWith("//")) 
	                {
	                	yorumSatir++;
	                } 
	                else 
	                {   
	                    // Fonksiyon sayısını kontrol et
	                    if (!inCommentBlock && line.matches(".*\\b\\w+\\s+\\w+\\(.*\\).*\\{?") && !line.contains("class ") && !line.contains("new ") && !line.endsWith(");")) 
	                    {
	                    	fonksiyonSayisi++;
	                        
	                    }
	                }

	                // Javadoc kapatma işaretini kontrol et
	                if (line.startsWith("*/") && inJavadoc == true) 
	                {
	                    inJavadoc = false;
	                }
	                else if (line.startsWith("*") && inJavadoc == true)
	                {
	                	javadocSatir++;
	                }
	                
	                // Yorum satırı kapatma işaretini kontrol et
	                if (line.startsWith("*/") && inCommentBlock == true) 
	                {
	                	inCommentBlock = false;
	                }
	                else if (line.startsWith("*") && inCommentBlock == true)
	                {
	                	yorumSatir++;
	                }
	                else if (!line.startsWith("//") && line.contains("//") && inJavadoc == false) 
	                {
	                	kodSatir++;
	                	yorumSatir++;
	                }
	                // Yorum satırı olup olmadığını kontrol et (YUKARIDAKI SATIR)
	                
	                // Bos satir olup olmadigini kontrol et
	                if(line.isEmpty())
	                {
	                	bosSatir++;
	                }
	                
	                // kodSatiri kontrolü
	                if(line.contains("/**") || line.contains("*/") || line.contains("/*"))
	                {
	                	kodSatir--;
	                }
	                
	                
	                toplamSatir++;
	            
	            }
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        }
	        
	        kodSatir = kodSatir + (toplamSatir - (yorumSatir + bosSatir + javadocSatir));
	        
	        
	        // Yorum sapma yüzdesi
	        double YG = (((double)javadocSatir + (double)yorumSatir)*0.8)/(double)fonksiyonSayisi;
	        double YH = ((double)kodSatir/(double)fonksiyonSayisi)*0.3;
	        double yorumSapmaYuzdesi = ((100*YG)/YH)-100;
	        	        
	        
	        System.out.println("Javadoc Satır Sayısı: " + javadocSatir);
	        System.out.println("Yorum Satır Sayısı: " + yorumSatir);
	        System.out.println("Kod Satır Sayısı: " + kodSatir);
	        System.out.println("LOC: " + toplamSatir);
	        System.out.println("Fonksiyon Sayısı: " + fonksiyonSayisi);
	        System.out.println("Yorum Sapma Yüzdesi : %" + yorumSapmaYuzdesi);
	        System.out.println("------------------------------------------------");
	    }	
	    
	    // Depo klasörünü silme metodu
	    private static void deleteDirectory(File directory) 
	    {
	        if (directory.isDirectory()) 
	        {
	            File[] files = directory.listFiles();
	            if (files != null) 
	            {
	                for (File file : files) 
	                {
	                    deleteDirectory(file);
	                }
	            }
	        }
	        // Dizini sil
	        directory.delete();
	    }
	}