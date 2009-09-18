/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.hpi.bpmn2_0.transformation;

import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
import de.hpi.bpmn2_0.factory.BPMNElement;
import de.hpi.bpmn2_0.factory.annotations.StencilId;
import de.hpi.bpmn2_0.model.BaseElement;
import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.model.ExclusiveGateway;
import de.hpi.bpmn2_0.model.FlowElement;
import de.hpi.bpmn2_0.model.FlowNode;
import de.hpi.bpmn2_0.model.Process;
import de.hpi.bpmn2_0.model.SequenceFlow;
import de.hpi.bpmn2_0.model.diagram.BpmnConnectorType;
import de.hpi.bpmn2_0.model.diagram.BpmnNode;
import de.hpi.bpmn2_0.model.diagram.LaneCompartment;
import de.hpi.bpmn2_0.model.diagram.ProcessDiagram;
import de.hpi.bpmn2_0.model.diagram.SequenceFlowConnector;
import de.hpi.util.reflection.ClassFinder;

/**
 * Converter class for Diagram to BPMN 2.0 transformation.
 * 
 * @author Philipp Giese
 * @author Sven Wagner-Boysen
 * 
 */
public class Diagram2BpmnConverter {
	/* Hash map of factories for BPMN 2.0 element to enable lazy initialization */
	private HashMap<String, AbstractBpmnFactory> factories;
	private HashMap<String, BPMNElement> bpmnElements;
	private Diagram diagram;
	
	/* Define edge ids */
	private final static String[] edgeIdsArray = {
		"SequenceFlow", 
		"Association_Undirected"
	};
	
	public final static HashSet<String> edgeIds = new HashSet<String>(Arrays.asList(edgeIdsArray));

	public Diagram2BpmnConverter(Diagram diagram) {
		this.factories = new HashMap<String, AbstractBpmnFactory>();
		this.bpmnElements = new HashMap<String, BPMNElement>();
		this.diagram = diagram;
	}

	/**
	 * Retrieves the stencil id related hashed factory.
	 * 
	 * @param stencilId
	 *            The stencil id
	 * @return The related factory
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private AbstractBpmnFactory getFactoryForStencilId(String stencilId)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		/* Create a new factory instance if necessary */
		if (!factories.containsKey(stencilId)) {
			this.factories.put(stencilId, createFactoryForStencilId(stencilId));
		}

		return this.factories.get(stencilId);
	}

	/**
	 * Creates a new factory instance for a stencil id.
	 * 
	 * @param stencilId
	 *            The stencil id
	 * @return The created factory
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * 
	 */
	private AbstractBpmnFactory createFactoryForStencilId(String stencilId)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		List<Class<? extends AbstractBpmnFactory>> factoryClasses = ClassFinder
				.getClassesByPackageName(AbstractBpmnFactory.class,
						"de.hpi.bpmn2_0.factory");

		/* Find factory for stencil id */
		for (Class<? extends AbstractBpmnFactory> factoryClass : factoryClasses) {
			StencilId stencilIdA = (StencilId) factoryClass
					.getAnnotation(StencilId.class);
			if(stencilIdA == null) continue;
			
			/* Check if appropriate stencil id is contained */
			List<String> stencilIds = Arrays.asList(stencilIdA.value());
			if (stencilIds.contains(stencilId)) {
				return (AbstractBpmnFactory) factoryClass.newInstance();
			}
		}

		throw new ClassNotFoundException("Factory for stencil id not found!");
	}

	/**
	 * Secures uniqueness of an BPMN Element.
	 * 
	 * @param el
	 * @throws InvalidKeyException
	 */
	private void addBpmnElement(BPMNElement el) throws InvalidKeyException {
		if (this.bpmnElements.containsKey(el.getId())) {
			throw new InvalidKeyException(
					"Key already exists for BPMN element!");
		}

		this.bpmnElements.put(el.getId(), el);
	}

	/**
	 * Retrieves the edges and updates the source and target references.
	 */
	private void detectConnectors() {
		for (Shape shape : this.diagram.getShapes()) {
			if (!edgeIds.contains(shape.getStencilId())) {
				continue;
			}
			
			/* Retrieve connector element */
			BPMNElement bpmnConnector = this.bpmnElements.get(shape
					.getResourceId());

			BPMNElement source = null;
			
			/* 
			 * Find source of connector. It is assumed that the first none edge
			 * element is the source element.
			 */
			for (Shape incomingShape : shape.getIncomings()) {
				if (edgeIds.contains(incomingShape.getStencilId())) {
					continue;
				}

				source = this.bpmnElements.get(incomingShape.getResourceId());
				break;
			}

			BPMNElement target = (shape.getTarget() != null) ? this.bpmnElements
					.get(shape.getTarget().getResourceId())
					: null;

			/* Update source references */
			if (source != null) {
				FlowNode sourceNode = (FlowNode) source.getNode();
				sourceNode.getOutgoing().add(
						(FlowNode) bpmnConnector.getNode());

				SequenceFlow seqFlow = (SequenceFlow) bpmnConnector.getNode();
				seqFlow.setSourceRef(sourceNode);

				BpmnConnectorType seqConnector = (BpmnConnectorType) bpmnConnector
						.getShape();
				seqConnector.setSourceRef(source.getShape());
			}

			/* Update target references */
			if (target != null) {
				FlowNode targetNode = (FlowNode) target.getNode();
				targetNode.getIncoming().add(
						(FlowNode) bpmnConnector.getNode());

				SequenceFlow seqFlow = (SequenceFlow) bpmnConnector.getNode();
				seqFlow.setTargetRef(targetNode);

				BpmnConnectorType seqConnector = (BpmnConnectorType) bpmnConnector
						.getShape();
				seqConnector.setTargetRef(target.getShape());
			}
		}
	}
	
	private void setDefaultSequenceFlowOfExclusiveGateway() {
		for(BPMNElement element : this.bpmnElements.values()) {
			BaseElement base = element.getNode();
			if(base instanceof ExclusiveGateway) {
				((ExclusiveGateway) base).findDefaultSequenceFlow();
			}
		}
	}

	/**
	 * Retrieves a BPMN 2.0 diagram and transforms it into the BPMN 2.0 model.
	 * 
	 * @param diagram
	 *            The BPMN 2.0 {@link Diagram} based on the ORYX JSON.
	 * @return The definitions root element of the BPMN 2.0 model.
	 * @throws BpmnConverterException
	 */
	public Definitions getDefinitionsFormDiagram()
			throws BpmnConverterException {
		/* Build-up standard definitions */
		Process process = new Process();
		process.setId("testProzess");
		LaneCompartment laneComp = new LaneCompartment();
		ProcessDiagram processDia = new ProcessDiagram();
		processDia.getLaneCompartment().add(laneComp);
		processDia.setProcessRef(process);

		Definitions definitions = new Definitions();
		definitions.getDiagram().add(processDia);
		definitions.getRootElement().add(process);

		/* Convert shapes to BPMN 2.0 elements */

		for (Shape shape : this.diagram.getChildShapes()) {
			try {
				AbstractBpmnFactory factory = this.getFactoryForStencilId(shape
						.getStencilId());
				BPMNElement bpmnElement = factory.createBpmnElement(shape);

				this.addBpmnElement(bpmnElement);

				process.getFlowElement().add(
						(FlowElement) bpmnElement.getNode());

				Object shapeToAdd = bpmnElement.getShape();
				if (shapeToAdd instanceof BpmnNode) {
					laneComp.getBpmnShape().add((BpmnNode) shapeToAdd);
				} else if (shapeToAdd instanceof SequenceFlowConnector) {
					processDia.getSequenceFlowConnector().add(
							(SequenceFlowConnector) shapeToAdd);
				}

			} catch (Exception e) {
				/* Pack exceptions in a BPMN converter exception */
				e.printStackTrace();
				throw new BpmnConverterException(
						"Error while converting to BPMN model", e.getCause());
			}
		}
		
		this.detectConnectors();
		this.setDefaultSequenceFlowOfExclusiveGateway();

		return definitions;
	}

	
	/* Getter & Setter */
	
	/**
	 * @return The list of BPMN 2.0 's stencil set edgeIds
	 */
	public static HashSet<String> getEdgeIds() {
		return edgeIds;
	}

}
