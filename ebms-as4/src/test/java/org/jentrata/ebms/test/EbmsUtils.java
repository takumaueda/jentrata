package org.jentrata.ebms.test;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ebms Test Utility Class
 *
 * @author aaronwalker
 */
public class EbmsUtils {

    private EbmsUtils(){}

    public static final SOAPMessage createSOAP11MessageFromClasspath(String filename) throws Exception {
        String message = toStringFromClasspath(filename);
        return createSOAP11Message(message);
    }

    public static final SOAPMessage createSOAP12MessageFromClasspath(String filename) throws Exception {
        String message = toStringFromClasspath(filename);
        return createSOAP12Message(message);
    }

    public static final SOAPMessage createSOAP12Message(String ebmsMessage) throws Exception {
        return createSOAPMessage(SOAPConstants.SOAP_1_2_PROTOCOL,ebmsMessage);
    }

    public static final SOAPMessage createSOAP11Message(String ebmsMessage) throws Exception {
        return createSOAPMessage(SOAPConstants.SOAP_1_1_PROTOCOL,ebmsMessage);
    }

    public static final SOAPMessage createSOAPMessage(String soapProtocol, String ebmsMessage) throws Exception{
        MessageFactory messageFactory = MessageFactory.newInstance(soapProtocol);
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        soapMessage.getSOAPPart().addMimeHeader("Content-ID", "<soapPart@jentrata.org>");
        SOAPHeader soapHeader = soapEnvelope.getHeader();

        Element ebms = toXML(ebmsMessage).getDocumentElement();
        Node node = soapEnvelope.getOwnerDocument().importNode(ebms,true);
        soapHeader.appendChild(node);
        soapMessage.saveChanges();

        return soapMessage;
    }

    public static void addAttachment(SOAPMessage soapMessage,String contentId,String contentType,InputStream content) throws Exception {
        addAttachment(soapMessage, contentId, contentType, content, new HashMap<String, String>());
    }

    public static void addAttachment(SOAPMessage soapMessage,String contentId,String contentType,InputStream content,Map<String,String> headers) throws Exception {
        AttachmentPart attachmentPart = soapMessage.createAttachmentPart();
        attachmentPart.setContentId(contentId);
        attachmentPart.setContentType(contentType);
        attachmentPart.setRawContent(content,contentType);
        for(Map.Entry<String,String> header : headers.entrySet()) {
            attachmentPart.addMimeHeader(header.getKey(), header.getValue());
        }
        soapMessage.addAttachmentPart(attachmentPart);
        soapMessage.saveChanges();
        soapMessage.getMimeHeaders().setHeader("Content-Type",soapMessage.getMimeHeaders().getHeader("Content-Type")[0] + "; start=\"<soapPart@jentrata.org>\"");

    }

    public static String toString(SOAPMessage soapMessage) throws Exception {
        return new String(toByteArray(soapMessage));
    }

    public static String toString(Document doc) throws TransformerException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        elementToStream(doc.getDocumentElement(), baos);
        return new String(baos.toByteArray());
    }

    public static byte[] toByteArray(SOAPMessage soapMessage) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        soapMessage.writeTo(bos);
        return bos.toByteArray();
    }

    public static void elementToStream(Element element, OutputStream out) throws TransformerException {
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(out);
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        transformer.transform(source, result);
    }

    public static final Document toXML(String xmlString) throws Exception {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder.parse(new ByteArrayInputStream(xmlString.getBytes()));
    }

    public static String toStringFromClasspath(String filename)  throws IOException {
        File file = fileFromClasspath(filename);
        return IOUtils.toString(new FileInputStream(file));
    }

    public static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
