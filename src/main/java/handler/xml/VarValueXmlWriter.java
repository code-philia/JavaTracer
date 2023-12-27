package handler.xml;

import static handler.xml.VarValueXmlConstants.FIELD_VAR_DECLARING_TYPE;
import static handler.xml.VarValueXmlConstants.FIELD_VAR_IS_STATIC;
import static handler.xml.VarValueXmlConstants.LOCAL_VAR_LINE_NUMBER;
import static handler.xml.VarValueXmlConstants.LOCAL_VAR_LOCATION_CLASS;
import static handler.xml.VarValueXmlConstants.VALUE_ARR_COMPONENT_TYPE_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_CHILDREN_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_CHILDREN_SEPARATOR;
import static handler.xml.VarValueXmlConstants.VALUE_ID_ATT;
import static handler.xml.VarValueXmlConstants.VALUE_IS_ARRAY_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_IS_ROOT_ATT;
import static handler.xml.VarValueXmlConstants.VALUE_REF_IS_NULL_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_REF_UNIQUE_ID_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_STRING_VALUE_PROP;
import static handler.xml.VarValueXmlConstants.VALUE_TAG;
import static handler.xml.VarValueXmlConstants.VALUE_VAR_TYPE_ATT;
import static handler.xml.VarValueXmlConstants.VARIABLE_TAG;
import static handler.xml.VarValueXmlConstants.VAR_ALIAS_ID_ATT;
import static handler.xml.VarValueXmlConstants.VAR_CAT_ATT;
import static handler.xml.VarValueXmlConstants.VAR_ID_ATT;
import static handler.xml.VarValueXmlConstants.VAR_NAME_ATT;
import static handler.xml.VarValueXmlConstants.VAR_TYPE_ATT;
import static handler.xml.VarValueXmlConstants.VAR_VALUES_TAG;

import exceptions.SavRtException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import model.value.ArrayValue;
import model.value.PrimitiveValue;
import model.value.ReferenceValue;
import model.value.VarValue;
import model.variable.FieldVar;
import model.variable.LocalVar;
import model.variable.Variable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.CollectionUtils;
import utils.StringUtils;

/**
 * @author thilyly_tran example: <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <variableValues>
 * <value id="1" isRoot="true" type="sample.Sample">
 * <variable id="951:3" name="s" type="LocalVar" varType="sample.Sample">
 * <lineNumber>21</lineNumber>
 * <locationClass>sample.Sample</locationClass>
 * </variable>
 * <stringValue>[field=0,]</stringValue>
 * <uniqueId>951</uniqueId>
 * <childIds>2</childIds>
 * </value>
 * <value id="2" isRoot="false" type="int">
 * <variable id="951.field:3" name="field" type="FieldVar" varType="int">
 * <isStatic>false</isStatic>
 * </variable>
 * <stringValue>0</stringValue>
 * </value>
 * </variableValues>
 */
public class VarValueXmlWriter {

    private final Collection<VarValue> varValues;

    public VarValueXmlWriter(Collection<VarValue> varValues) {
        this.varValues = varValues;
    }

    public static String generateXmlContent(Collection<VarValue> varValues) {
        VarValueXmlWriter writer = new VarValueXmlWriter(varValues);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        writer.writeXml(outStream);
        return outStream.toString(StandardCharsets.UTF_8);
    }

    public void writeXml(OutputStream out) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            toXml(doc);
            doc.setXmlVersion("1.1");
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);
        } catch (Exception e) {
            throw new SavRtException(e);
        }
    }

    private Node toXml(Document doc) {
        XmlBuilder xmlBuilder = new XmlBuilder(doc);
        for (VarValue varValue : varValues) {
            xmlBuilder.appendVarValue(varValue);
        }
        return xmlBuilder.root;
    }

    private static class XmlBuilder {

        private final Document doc;
        private final Element root;
        private final Element valuesEle;
        private int valueIdCounter = 0;

        public XmlBuilder(Document doc) {
            this.doc = doc;
            root = doc.createElement(VAR_VALUES_TAG);
            valuesEle = root;
            doc.appendChild(root);
        }

        private String appendVarValue(VarValue varValue) {
            Element valueEle = addChild(valuesEle, VALUE_TAG);
            String valueId = generateValueId(varValue);
            addAttribute(valueEle, VALUE_ID_ATT, valueId);
            addAttribute(valueEle, VALUE_IS_ROOT_ATT, varValue.isRoot());
            addAttribute(valueEle, VALUE_VAR_TYPE_ATT, varValue.getType());
            /* variable */
            appendVariable(valueEle, varValue.getVariable());

            if (varValue instanceof ArrayValue arrayVal) {
                addProperty(valueEle, VALUE_ARR_COMPONENT_TYPE_PROP, arrayVal.getComponentType());
                addProperty(valueEle, VALUE_IS_ARRAY_PROP, true);
                addProperty(valueEle, VALUE_REF_IS_NULL_PROP, arrayVal.isNull());
            } else if (varValue instanceof ReferenceValue refVal) {
                addProperty(valueEle, VALUE_REF_UNIQUE_ID_PROP, refVal.getUniqueID());
                addProperty(valueEle, VALUE_REF_IS_NULL_PROP, refVal.isNull());
            } else if (varValue instanceof PrimitiveValue) {
                addValueStringValueProperty(valueEle, varValue.getStringValue());
            } else {
                addValueStringValueProperty(valueEle, varValue.getStringValue());
            }
            if (CollectionUtils.isNotEmpty(varValue.getChildren())) {
                List<String> childIds = new ArrayList<String>(varValue.getChildren().size());
                for (VarValue child : varValue.getChildren()) {
                    childIds.add(appendVarValue(child));
                }
                addProperty(valueEle, VALUE_CHILDREN_PROP,
                    StringUtils.join(childIds, VALUE_CHILDREN_SEPARATOR));
            }
            return valueId;
        }

        private void addValueStringValueProperty(Element valueEle, String strVal) {
            addProperty(valueEle, VALUE_STRING_VALUE_PROP, XmlFilter.filter(strVal), false);
        }


        private String generateValueId(VarValue varValue) {
            String id = Integer.toString(++valueIdCounter);
            return id;
        }

        public void appendVariable(Element parent, Variable variable) {
            Element varEle = addChild(parent, VARIABLE_TAG);
            addAttribute(varEle, VAR_TYPE_ATT, variable.getType());
            addAttribute(varEle, VAR_NAME_ATT, variable.getName());
            addAttribute(varEle, VAR_ID_ATT, variable.getVarID());
            addAttribute(varEle, VAR_ALIAS_ID_ATT, variable.getAliasVarID());
            addAttribute(varEle, VAR_CAT_ATT, variable.getClass().getSimpleName());
            if (variable instanceof FieldVar fieldVar) {
                addProperty(varEle, FIELD_VAR_IS_STATIC, fieldVar.isStatic());
                addProperty(varEle, FIELD_VAR_DECLARING_TYPE, fieldVar.getDeclaringType());
            } else if (variable instanceof LocalVar localVar) {
                addProperty(varEle, LOCAL_VAR_LINE_NUMBER, localVar.getLineNumber());
                addProperty(varEle, LOCAL_VAR_LOCATION_CLASS, localVar.getLocationClass());
            }
        }

        private Element addChild(Element parent, String tagName) {
            Element child = doc.createElement(tagName);
            parent.appendChild(child);
            return child;
        }

        private void addProperty(Element parent, String tagName, Object value) {
            addProperty(parent, tagName, value, false);
        }

        private void addProperty(Element parent, String tagName, Object value, boolean isCDATA) {
            if (value == null) {
                return;
            }
            Element propertyEle = addChild(parent, tagName);
            if (isCDATA) {
                propertyEle.appendChild(doc.createCDATASection(value.toString()));
            } else {
                propertyEle.appendChild(doc.createTextNode(value.toString()));
            }
        }

        private void addAttribute(Element element, String attName, Object attVal) {
            if (attVal == null) {
                return;
            }
            Attr nameAttr = doc.createAttribute(attName);
            nameAttr.setValue(attVal.toString());
            element.setAttributeNode(nameAttr);
        }

    }
}
