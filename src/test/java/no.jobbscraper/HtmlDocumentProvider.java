package no.jobbscraper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class HtmlDocumentProvider {

    private Document listViewDocument;
    private Document detailViewDocument;

    public HtmlDocumentProvider(
            String url,
            String listViewContentFileName,
            String detailViewFileName) {
        Path workingDir = Path.of("", "src/test/resources/html");
        try {
            String listViewContent = Files.readString(workingDir.resolve(listViewContentFileName));
            String detailViewContent = Files.readString(workingDir.resolve(detailViewFileName));

            this.listViewDocument = Jsoup.parse(listViewContent, url);
            this.detailViewDocument = Jsoup.parse(detailViewContent, url);
        } catch (IOException  e){
            e.printStackTrace();
        }
    }

    protected Document getListViewDocument(){
        return this.listViewDocument;
    }

    protected Document getDetailViewDocument(){
        return this.detailViewDocument;
    }
}
