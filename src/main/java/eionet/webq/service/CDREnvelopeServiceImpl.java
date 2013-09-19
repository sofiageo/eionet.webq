/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Questionnaires 2
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Anton Dmitrijev
 */
package eionet.webq.service;

import eionet.webq.dto.WebQMenuParameters;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 */
@Service
public class CDREnvelopeServiceImpl implements CDREnvelopeService {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(CDREnvelopeServiceImpl.class);
    /**
     * XML-RPC client.
     */
    @Autowired
    private XmlRpcClient xmlRpcClient;
    /**
     *  Get envelope xml files remote method name.
     */
    @Value("#{ws['cdr.envelope.get.xml.files']}")
    private String getEnvelopeXmlFilesMethod;

    @Override
    public MultiValueMap<String, XmlFile> getXmlFiles(WebQMenuParameters parameters) {
        try {
            Object xmlFilesMappedBySchema = xmlRpcClient.execute(buildConfig(parameters), getEnvelopeXmlFilesMethod, emptyList());
            return transformGetXmlFilesResponse(xmlFilesMappedBySchema);
        } catch (XmlRpcException e) {
            throw new CDREnvelopeException("Unable to call envelope XML-RPC service", e);
        }
    }

    /**
     * Transform raw envelope service response to usable form.
     *
     * @param response service response
     * @return {@link XmlFile} grouped by xml schema.
     */
    @SuppressWarnings("unchecked")
    private MultiValueMap<String, XmlFile> transformGetXmlFilesResponse(Object response) {
        LinkedMultiValueMap<String, XmlFile> result = new LinkedMultiValueMap<String, XmlFile>();
        if (response != null) {
            try {
                for (Map.Entry<String, Object[]> entry : ((Map<String, Object[]>) response).entrySet()) {
                    String xmlSchema = entry.getKey();
                    for (Object values : entry.getValue()) {
                        Object[] xmlFileData = (Object[]) values;
                        result.add(xmlSchema, new XmlFile(xmlFileData[0].toString(), xmlFileData[1].toString(), xmlSchema));
                    }
                }
            } catch (ClassCastException e) {
                LOGGER.error("received response=" + response);
                throw new CDREnvelopeException("unexpected response format from CDR envelope service.", e);
            }
        } else {
            LOGGER.warn("expected not null response from envelope service");
        }
        return result;
    }

    /**
     * Builds XxmlRpcClientConfig from {@link eionet.webq.dto.WebQMenuParameters}.
     *
     * @param parameters parameters
     * @return config
     */
    private XmlRpcClientConfig buildConfig(WebQMenuParameters parameters) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(createUrlFromString(parameters.getEnvelopeUrl()));
        if (parameters.isAuthorizationSet()) {
            config.setBasicUserName(parameters.getUserName());
            config.setBasicPassword(parameters.getPassword());
        }
        return config;
    }

    /**
     * Creates {@link URL} instance wrapping {@link java.net.MalformedURLException}.
     *
     * @param url string
     * @return url object
     */
    private URL createUrlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new CDREnvelopeException("Envelope URL is malformed", e);
        }
    }
}