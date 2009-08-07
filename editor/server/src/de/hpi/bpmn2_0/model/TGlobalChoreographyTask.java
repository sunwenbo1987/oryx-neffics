//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.07 at 02:01:49 PM CEST 
//


package de.hpi.bpmn2_0.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for tGlobalChoreographyTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tGlobalChoreographyTask">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schema.omg.org/spec/BPMN/2.0}tCallableElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://schema.omg.org/spec/BPMN/2.0}participant" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://schema.omg.org/spec/BPMN/2.0}messageFlow" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="initiatingParticipantRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tGlobalChoreographyTask", propOrder = {
    "participant",
    "messageFlow"
})
public class TGlobalChoreographyTask
    extends TCallableElement
{

    protected List<TParticipant> participant;
    protected List<TMessageFlow> messageFlow;
    @XmlAttribute
    protected QName initiatingParticipantRef;

    /**
     * Gets the value of the participant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParticipant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TParticipant }
     * 
     * 
     */
    public List<TParticipant> getParticipant() {
        if (participant == null) {
            participant = new ArrayList<TParticipant>();
        }
        return this.participant;
    }

    /**
     * Gets the value of the messageFlow property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageFlow property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageFlow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMessageFlow }
     * 
     * 
     */
    public List<TMessageFlow> getMessageFlow() {
        if (messageFlow == null) {
            messageFlow = new ArrayList<TMessageFlow>();
        }
        return this.messageFlow;
    }

    /**
     * Gets the value of the initiatingParticipantRef property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getInitiatingParticipantRef() {
        return initiatingParticipantRef;
    }

    /**
     * Sets the value of the initiatingParticipantRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setInitiatingParticipantRef(QName value) {
        this.initiatingParticipantRef = value;
    }

}
