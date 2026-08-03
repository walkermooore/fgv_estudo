package com.fgv.studyhub.service;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class TextChunker {
 private static final int TARGET=1000,MIN=800,MAX=1200;
 public List<Chunk> split(String text){List<Chunk> out=new ArrayList<>();String[] paragraphs=text.split("\\n\\s*\\n");StringBuilder current=new StringBuilder();String chapter=null;for(String raw:paragraphs){String p=raw.trim();if(p.isBlank())continue;if(isHeading(p))chapter=p;if(current.length()>0&&current.length()+p.length()+2>MAX){flush(out,current,chapter);}if(p.length()>MAX){int start=0;while(start<p.length()){int end=Math.min(start+TARGET,p.length());if(end<p.length()){int space=p.lastIndexOf(' ',end);if(space>start+MIN)end=space;}append(current,p.substring(start,end));if(current.length()>=MIN)flush(out,current,chapter);start=end;}}else append(current,p);if(current.length()>=TARGET)flush(out,current,chapter);}flush(out,current,chapter);return out;}
 private void append(StringBuilder b,String s){if(b.length()>0)b.append("\n\n");b.append(s.trim());}
 private void flush(List<Chunk> out,StringBuilder b,String chapter){if(b.length()>0){out.add(new Chunk(out.size(),chapter,b.toString()));b.setLength(0);}}
 private boolean isHeading(String p){return p.length()<120&&(p.matches("(?i)^(cap[ií]tulo|se[cç][aã]o|parte|t[ií]tulo).*" )||p.matches("^[A-ZÁÀÂÃÉÊÍÓÔÕÚÇ0-9 .:–-]{4,}$"));}
 public record Chunk(int index,String chapter,String content){public int tokenCount(){return Math.max(1,(int)Math.ceil(content.length()/4.0));}}
}
