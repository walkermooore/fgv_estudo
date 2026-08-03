package com.fgv.studyhub.service;
import com.fgv.studyhub.entity.MaterialType;
import com.fgv.studyhub.exception.ExtractionException;
import com.fgv.studyhub.validation.UrlSecurityValidator;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class TextExtractionService {
 private final WebClient.Builder webClientBuilder; private final UrlSecurityValidator urls;
 public String fromFile(Path path,MaterialType type){try{return clean(switch(type){case PDF->pdf(path);case DOCX->docx(path);case HTML->Jsoup.parse(Files.readString(path)).select("main,article,body").text();case TXT,MARKDOWN,CSV->Files.readString(path,StandardCharsets.UTF_8);default->throw new ExtractionException("Unsupported file type");});}catch(IOException e){throw new ExtractionException("Text extraction failed",e);}}
 public ExtractedUrl fromUrl(String raw){URI uri=urls.validate(raw);try{String html=webClientBuilder.build().get().uri(uri).retrieve().bodyToMono(String.class).timeout(Duration.ofSeconds(30)).block();if(html==null||html.isBlank())throw new ExtractionException("URL returned empty content");var doc=Jsoup.parse(html,uri.toString());doc.select("script,style,noscript,nav,header,footer,aside,form,iframe,.advertisement,.ads").remove();var main=doc.selectFirst("main,article,[role=main]");String text=clean((main==null?doc.body():main).text());return new ExtractedUrl(doc.title().isBlank()?uri.getHost():doc.title(),text,html.getBytes(StandardCharsets.UTF_8).length);}catch(ExtractionException e){throw e;}catch(Exception e){throw new ExtractionException("Could not download or extract the URL",e);}}
 private String pdf(Path p)throws IOException{try(var doc=Loader.loadPDF(p.toFile())){return new PDFTextStripper().getText(doc);}}
 private String docx(Path p)throws IOException{try(var in=Files.newInputStream(p);var doc=new XWPFDocument(in)){return doc.getParagraphs().stream().map(x->x.getText()).collect(Collectors.joining("\n"));}}
 private String clean(String text){String value=text.replace("\u0000","").replaceAll("[\\p{Cc}&&[^\\r\\n\\t]]","").replaceAll("[ \\t]+"," ").replaceAll("\\n{3,}","\n\n").trim();if(value.isBlank())throw new ExtractionException("No useful text could be extracted");return value;}
 public record ExtractedUrl(String title,String text,long size){}
}
