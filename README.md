# JobScraper

JobScraper is a Java application designed to scrape job postings from various websites. It utilizes web scraping techniques to extract relevant information such as job title, company name, description, and tags from job listing pages.
**With a bit of work this project can be turned into a 'whatever you want scraper'**.

## Features
- **Flexible Scraper**: The application provides a flexible scraper architecture, allowing easy integration of new scraping modules for different job listing websites.
- **Concurrent Scraping**: JobScraper leverages virtual threads to concurrently scrape job postings from multiple websites, improving performance and efficiency.
- **SQLite Database**: JobScraper utilizes an SQLite database to store visited URLs, ensuring that duplicate URLs are not processed during scraping.
- **REST API Integration**: The application supports integration with REST APIs, facilitating the seamless posting of job data to external endpoints.

## Installation
1. Clone the repository
```
git clone https://github.com/JonathanD01/job-scraper.git
```

2. Build the project
```
cd job-scraper
mvn clean package
```

3. Run the application:
```
// Run without sending data to the rest api
java -jar job-scraper-jar-with-dependencies.jar --drc yes

// or send
java -jar job-scraper-jar-with-dependencies.jar --drc no --ip 127.0.0.1 --p 8081 --path api/v1/jobposts --rp job_posts
```

### Arguments
| Name                  | Short name | Help                                                                                                                                                     |
|-----------------------|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| --help                | --h        | Display help                                                                                                                                             |
| --ip                  | --ip       | Specify the ip address                                                                                                                                   |
| --port                | --p        | Specify the port number                                                                                                                                  |
| --path                | --ph       | Specify the path                                                                                                                                         |
| --request-param       | --rp       | Specify the request parameter                                                                                                                            |
| --disable-rest-client | --drc      | Disable the REST client                                                                                                                                  |
| --start-page          | --sp       | All scrapers will start at the given page                                                                                                                |
| --disable-scrapers    | --ds       | Enter a comma separated list of the urls you wish to disable.<br>Example -> https://www.finn.no/job/fulltime/search.html,https://karrierestart.no/jobb   |

## Data Sent to Your REST API
The following data exemplifies the payload that can be directly transmitted to your REST API.
`job_posts` is equal to the `--request-param` argument.
```
{
   "job_posts":[
      {
         "url":"https://karrierestart.no/ledig-stilling/2535315",
         "company_name":"NCC",
         "company_image_url":"https://karrierestart.no/ImageSource/CompanyLogo160Src/334",
         "image_url":"https://karrierestart.no/UserFiles/company/334/media/...jpg",
         "title":"Anleggsleder",
         "description":"<p><strong>Vi søker etter Anleggsleder til Civil Engineering, Avdeling Øst,...</p>",
         "deadline":"21.04.2024",
         "job_tags":[
            "Oslo",
            "NCC",
            "Anleggsleder"
         ],
         "job_definitions":{
            "Stillingstype":[
               "Fast ansettelse",
               "Heltid"
            ],
            "Tiltredelse":[
               "Etter avtale"
            ]
         }
      },
      {
         "url":"https://karrierestart.no/ledig-stilling/2535314",
         "company_name":"NCC",
         "company_image_url":"https://karrierestart.no/ImageSource/CompanyLogo160Src/334",
         "image_url":"https://karrierestart.no/UserFiles/company/334/media/cache/...jpg",
         "title":"NCC Industry søker erfaren Borerigg-operatør/Bergsprenger",
         "description":"<p><strong>NCC Industry</strong> tilbyr produkter og tjenester...</p>",
         "deadline":"14.04.2024",
         "job_tags":[
            "Kragerø",
            "NCC",
            "Industry",
            "søker"
         ],
         "job_definitions":{
            "Stillingstype":[
               "Fast ansettelse",
               "Heltid"
            ],
            "Tiltredelse":[
               "Etter avtale"
            ]
         }
      }
   ]
}
```

## Creating your own scraper
It is very simple to create your own scraper. Look at how other scraper classes have implemented their logic.
Make sure `BaseWebScraper` permits your new scraper class.
Begin by creating a new Java class for your scraper. Ensure that your class extends BaseWebScraper and is structured to permit integration with the existing framework. Here's a basic template to get you started:

### Scraper class
The XPath "//article" is used here as a generalized example to represent a common scenario on job board websites where job listings are enclosed within "article" tags.
It efficiently captures job postings typically presented in this format in HTML documents.
However, it's important to note that this XPath may not universally apply to all job board websites,
but it serves as a practical example for demonstration purposes in this context.
```
public final class MyScraperClass extends BaseWebScraper {
    
    public MyScraperClass() {
        super(WebsiteURL.MYWEBSITE_ORG_PAGE, WebsiteURL.MYWEBSITE_ORG_PAGE_WITH_PAGE, "//article");
    }

    @Override
    String extractUrlForJobPostFromElement(String url, Element element) {
        // Implement logic
        return null;
    }

    @Override
    String extractImageUrlForJobPostFromElement(String url, Element element) {
        // Implement logic
        return null;
    }

    // Continue to implement other methods
}
```

### Integrate your scraper:
Go to the Main class and add your scraper to the set of scrapers
```
Set<BaseWebScraper> webScrapers = Set.of(
    new ArbeidsplassenNavScraper(),
    new KarriereStartScraper(),
    new FinnScraper(),
    new MyScraperClass() // my new shiny scraper wohoo
);
```