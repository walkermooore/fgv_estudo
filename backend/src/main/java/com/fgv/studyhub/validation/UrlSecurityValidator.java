package com.fgv.studyhub.validation;
import com.fgv.studyhub.exception.BadRequestException;
import org.springframework.stereotype.Component;
import java.net.*;
@Component
public class UrlSecurityValidator {
 public URI validate(String raw){try{URI uri=URI.create(raw);if(!("http".equalsIgnoreCase(uri.getScheme())||"https".equalsIgnoreCase(uri.getScheme()))||uri.getHost()==null)throw new BadRequestException("A valid HTTP(S) URL is required");for(InetAddress address:InetAddress.getAllByName(uri.getHost()))if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||address.isSiteLocalAddress())throw new BadRequestException("Private network URLs are not allowed");return uri;}catch(IllegalArgumentException|UnknownHostException e){throw new BadRequestException("Invalid or unreachable URL");}}
}
