package fr.opensagres.xdocreport.document.preprocessor.dom;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.io.XDocArchive;
import fr.opensagres.xdocreport.core.utils.DOMUtils;
import fr.opensagres.xdocreport.document.preprocessor.AbstractXDocPreprocessor;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.IDocumentFormatter;

public abstract class DOMPreprocessor
    extends AbstractXDocPreprocessor<Document>
{

    @Override
    protected Document getSource( XDocArchive documentArchive, String entryName )
        throws XDocReportException, IOException
    {

        try
        {
            return DOMUtils.load( documentArchive.getEntryInputStream( entryName ) );
        }
        catch ( ParserConfigurationException e )
        {
            throw new XDocReportException( e );
        }
        catch ( SAXException e )
        {
            throw new XDocReportException( e );
        }
    }

    @Override
    protected void closeSource( Document reader )
    {

    }

    @Override
    public boolean preprocess( String entryName, Document document, Writer writer, FieldsMetadata fieldsMetadata,
                               IDocumentFormatter formatter, Map<String, Object> sharedContext )
        throws XDocReportException, IOException
    {
        try
        {
            visit( document, entryName, fieldsMetadata, formatter, sharedContext );
            DOMUtils.save( document, writer );
            return true;
        }
        catch ( TransformerException e )
        {
            throw new XDocReportException( e );
        }
    }

    protected abstract void visit( Document document, String entryName, FieldsMetadata fieldsMetadata,
                                   IDocumentFormatter formatter, Map<String, Object> sharedContext )
        throws XDocReportException;

    /**
     * @param element
     * @param attrName
     * @param contextKey
     * @param formatter
     */
    protected void updateDynamicAttr( Element element, String attrName, String contextKey, IDocumentFormatter formatter )
    {
        if ( element.hasAttribute( attrName ) )
        {

            element.setAttribute( attrName, getDynamicAttr( element, attrName, contextKey, contextKey, formatter ) );

        }
    }

    /**
     * @param element
     * @param attrName
     * @param contextKey
     * @param formatter
     */
    protected void updateDynamicAttr( Element element, String attrName, String contextIfKey, String contextValueKey,
                                      IDocumentFormatter formatter )
    {
        if ( element.hasAttribute( attrName ) )
        {

            element.setAttribute( attrName,
                                  getDynamicAttr( element, attrName, contextIfKey, contextValueKey, formatter ) );

        }
    }

    /**
     * Generate directive (ex with FM: [#if ___font??]${___font}[#else]Arial[/#if])
     * 
     * @param element
     * @param attrName
     * @param formatter
     * @return
     */
    protected String getDynamicAttr( Element element, String attrName, String contextIfKey, String contextValueKey,
                                     IDocumentFormatter formatter )
    {

        StringBuilder value = new StringBuilder();
        value.append( formatter.getStartIfDirective( contextIfKey ) );       
        value.append( formatter.formatAsSimpleField( true, contextValueKey ) );
        value.append( formatter.getElseDirective() );
        value.append( element.getAttribute( attrName ) );
        value.append( formatter.getEndIfDirective( contextIfKey ) );
        return value.toString();
    }
}
