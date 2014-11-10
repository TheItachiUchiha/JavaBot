package com.gmail.inverseconduit.chat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.datatype.JSONChatEvents;
import com.google.gson.Gson;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class JSONChatConnection extends WebConnectionWrapper {

    private boolean           isEnabled = false;

    private StackExchangeChat seBrowser;

    private Gson              gson      = new Gson();

    public JSONChatConnection(WebClient webClient, StackExchangeChat seBrowser) throws IllegalArgumentException {
        super(webClient);
        this.seBrowser = seBrowser;
    }

    @Override
    public WebResponse getResponse(WebRequest request) throws IOException {
        final WebResponse response = super.getResponse(request);
        if (isEnabled) {
            final String rString = response.getContentAsString();
            System.out.println(StringUtils.abbreviate("rString: " + rString, 50));
            if ( !rString.contains("{\"events\":"))
                return response;
            try {
                final String jsonString = rString.substring(rString.indexOf(":") + 1, rString.lastIndexOf("}"));
                System.out.println(StringUtils.abbreviate("jsonString: " + jsonString, 50));
                final JSONChatEvents events = gson.fromJson(jsonString, JSONChatEvents.class);
                events.setSite(SESite.fromUrl(request.getUrl()));
                seBrowser.handleChatEvents(events);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
