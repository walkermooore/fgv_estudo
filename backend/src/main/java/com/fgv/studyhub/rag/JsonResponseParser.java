package com.fgv.studyhub.rag;
import com.fasterxml.jackson.databind.*;
import com.fgv.studyhub.exception.AiParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class JsonResponseParser {
 private final ObjectMapper mapper;
 public <T>T parseFirstObject(String raw,Class<T> type){
  if(raw==null||raw.isBlank()) throw new AiParsingException("AI returned an empty response");
  Exception lastError=null;
  for(int start=0;start<raw.length();start++){
   if(raw.charAt(start)!='{')continue;
   String candidate=balancedObject(raw,start);
   if(candidate==null)continue;
   try{return mapper.readValue(candidate,type);}catch(Exception e){lastError=e;}
  }
  throw new AiParsingException("AI response contains no valid JSON object",lastError);
 }
 private String balancedObject(String raw,int start){StringBuilder out=new StringBuilder();int depth=0;boolean quoted=false,escaped=false;for(int i=start;i<raw.length();i++){char c=raw.charAt(i);out.append(c);if(quoted){if(escaped)escaped=false;else if(c=='\\')escaped=true;else if(c=='\"')quoted=false;continue;}if(c=='\"'){quoted=true;continue;}if(c=='{')depth++;else if(c=='}'&&--depth==0)return out.toString();}return null;}
}
