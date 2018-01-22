package com.bpmncheck.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.EndEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.ExclusiveGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.InclusiveGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.ParallelGatewayImpl;
import org.camunda.bpm.model.bpmn.impl.instance.StartEventImpl;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import com.bpmncheck.model.Log;

public class BpmnService {

	public BpmnModelInstance readFile(String path) {
		File file = new File(path);
		BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);
		return modelInstance;
	}

	public List<Log> checkAllBpmnLogs(BpmnModelInstance modelBpmn, List<Log> logs) {
		for (Log log : logs) {
			System.out.println("*********************" + log.getId());
			log.setResult(checkBpmnLog(modelBpmn, log));
		}
		return logs;
	}

	public boolean checkBpmnLog(BpmnModelInstance modelBpmn, Log log) {
		/*
		 * Collection de todos os nós do Bpmn (Nó são Gateways e Tasks do Bpmn)
		 */
		Collection<FlowNode> flowNodes = modelBpmn.getModelElementsByType(FlowNode.class);

		/*
		 * Busca dentro da lista de nós o Evento Inicial Busca feita pela tipo de classe
		 * que é implementado para o caso Evento Inicial = StartEventImpl.class
		 */
		FlowNode startNode = null;
		for (FlowNode flowNode : flowNodes) {
			if (flowNode.getClass() == StartEventImpl.class) {
				startNode = flowNode;
				break;
			}
		}

		/*
		 * Adiciona o nó inicial a lista de nós correntes, para iniciar a checagem
		 */
		List<FlowNode> currentNodes = new ArrayList<>();
		currentNodes.add(startNode);
		
		/*
		 * Lista para a chamada recursiva
		 */
		List<String> events = new ArrayList<>(log.getEvents());

		/*
		 * Executa uma checagem no modelo seguindo a lista de eventos do log
		 */
		return checkBpmnLogRecursive(modelBpmn, events, currentNodes);
	}

	private Boolean checkBpmnLogRecursive(BpmnModelInstance modelBpmn, List<String> logEvents, List<FlowNode> currentNodes) {
		List<FlowNode> newCurrentNodes = new ArrayList<>();
		List<String> logEventsCopy = new ArrayList<>(logEvents);
		
		// Verifica se a lista de novos nós é vazia, se for retorna falso
		if (currentNodes.size() == 0) return false;

		// Verifica se todos os nós são finais
		if(checkAlwaysEndEvent(currentNodes) && (logEvents.size() == 0)) {
			return true;
		} else if (checkAlwaysEndEvent(currentNodes) && (logEvents.size() != 0)) {
			return false;
		}
		
		/*
		 * Basicamente se todos os nós forem tarefas, verifica se eles estão
		 * imediatamente na lista de logEvents, não importando a ordem apresentada
		 * e sim que eles sejam os próximos da lista, quando é econtrado na sequencia
		 * do log, ele já busca as saídas daquele nó para adicionar na lista de novos
		 * nós para a proxima chamada recursiva. Se não for uma lista de tarefas, é lido
		 * cada nó e aplicado a lógica conforme o caso especifico de cada.
		 */
		if (checkAlwaysTaskAndEndEvent(currentNodes)) {
			
			for (Iterator<FlowNode> node = currentNodes.iterator(); node.hasNext();) {
				FlowNode currentNode = node.next();
				for (int i = 0; i < countTasks(currentNodes); i++) {
					if(i < logEvents.size()) {
						System.out.println("** " + currentNode.getName());
						if (currentNode.getName().equals(logEvents.get(i))) {
							System.out.println(currentNode.getName());
							logEvents.remove(i);
							
							Collection<SequenceFlow> outgoing = currentNode.getOutgoing();
							Iterator<SequenceFlow> iterator = outgoing.iterator();
							while (iterator.hasNext()) {
								SequenceFlow sequence = iterator.next();
								FlowNode newCurrentNode = sequence.getTarget();
								if (!newCurrentNodes.contains(newCurrentNode)) {
									newCurrentNodes.add(newCurrentNode);
								}
							}
							
							node.remove();
						} 
					}
				}
			}
			
			if((currentNodes.size() > 0) && (!checkAlwaysEndEvent(currentNodes))) {
				return false;
			} else {
				newCurrentNodes.addAll(currentNodes);
				return checkBpmnLogRecursive(modelBpmn, logEvents, newCurrentNodes);
			}
		} else {
			for(Iterator<FlowNode> node = currentNodes.iterator(); node.hasNext();) {
				FlowNode currentNode = node.next();
				
				/*
				 * TASK
				 * Simplesmente adiciona novamente na lista de novos nós
				 */
				if (currentNode.getClass() == TaskImpl.class) {
					newCurrentNodes.add(currentNode);
				} else {
					Collection<SequenceFlow> outgoing = currentNode.getOutgoing();
					Iterator<SequenceFlow> iterator = outgoing.iterator();
					
					/*
					 * PARALLEL OR START
					 * Adiciona todas as saídas na lista de novos nós
					 */
					if(currentNode.getClass() == ParallelGatewayImpl.class || currentNode.getClass() == StartEventImpl.class) {
						while (iterator.hasNext()) {
							SequenceFlow sequence = iterator.next();
							FlowNode newCurrentNode = sequence.getTarget();
							if (!newCurrentNodes.contains(newCurrentNode)) {
								newCurrentNodes.add(newCurrentNode);
							}
						}
					}
					
					/*
					 * EXCLUSIVE
					 * Testa cada saída até encontrar uma correta, usando a recursão
					 */
					if(currentNode.getClass() == ExclusiveGatewayImpl.class) {
						while(iterator.hasNext()) {
							SequenceFlow sequence = iterator.next();
							FlowNode newCurrentNode = sequence.getTarget();
							
							/*
							 * Copia para caso não retorne verdadeiro continuar do ponto em que parou
							 */
							List<FlowNode> newCurrentNodesCopy = new ArrayList<>(newCurrentNodes);
							
							if (!newCurrentNodes.contains(newCurrentNode)) {
								newCurrentNodes.add(newCurrentNode);
							}
							
							for(int i = (currentNodes.indexOf(currentNode) + 1); i < currentNodes.size(); i++) {
								newCurrentNodes.add(currentNodes.get(i));
							}
							
							if (checkBpmnLogRecursive(modelBpmn, logEvents, newCurrentNodes)) {
								return true;
							} else {
								newCurrentNodes = new ArrayList<>(newCurrentNodesCopy);
								logEvents = new ArrayList<>(logEventsCopy);
							}
						}
						return false;
					}
					
					/*
					 * INCLUSIVE
					 * Testa as combinações possíveis de saídas, usando a recursão
					 */
					if(currentNode.getClass() == InclusiveGatewayImpl.class) {
						List<FlowNode> newInclusiveNodes = new ArrayList<>();
						
						Collection<SequenceFlow> outgoingInclusive = currentNode.getOutgoing();
						Iterator<SequenceFlow> iteratorInclusive = outgoingInclusive.iterator();
						while (iteratorInclusive.hasNext()) {
							SequenceFlow sequence = iteratorInclusive.next();
							FlowNode newInclusiveNode = sequence.getTarget();
							if (!newInclusiveNodes.contains(newInclusiveNode)) {
								newInclusiveNodes.add(newInclusiveNode);
							}
						}
						
				        InclusiveComb comb = new InclusiveComb(newInclusiveNodes, 0);
				        while ( comb.hasNext() ) {
				        		List<FlowNode> listCombInclusiveNodes = new ArrayList<>();
				        		listCombInclusiveNodes = comb.next() ;
				        		
				        		List<FlowNode> newCurrentNodesCopy = new ArrayList<>(newCurrentNodes);
				        		
				            for ( FlowNode e : listCombInclusiveNodes ) {
								newCurrentNodes.add(e);
				            }
				            
				            for(int i = (currentNodes.indexOf(currentNode) + 1); i < currentNodes.size(); i++) {
								newCurrentNodes.add(currentNodes.get(i));
							}
				            
				            if (checkBpmnLogRecursive(modelBpmn, logEvents, newCurrentNodes)) {
								return true;
							} else {
								newCurrentNodes = new ArrayList<>(newCurrentNodesCopy);
								logEvents = new ArrayList<>(logEventsCopy);
							}
				        }
				        return false;
					}
					
					/*
					 * END
					 * Adiciona a lista de novos nós
					 */
					if(currentNode.getClass() == EndEventImpl.class) {
						if (!newCurrentNodes.contains(currentNode)) {
							newCurrentNodes.add(currentNode);
						}
					}
				}
			}
		}
		return checkBpmnLogRecursive(modelBpmn, logEvents, newCurrentNodes);
	}
	
	/*
	 * Conta quantas são as tarefas na lista (pode ter END Event no meio, por isso foi necessário esse método)
	 */
	private Integer countTasks(List<FlowNode> nodes) {
		Integer count = 0;
		for (FlowNode node : nodes) {
			if (node.getClass() == TaskImpl.class) {
				count++;
			}
		}
		return count;
	}
	
	/*
	 * Verifica se todos os nós da lista são tarefas, com a resalva de ter um END no meio da lista
	 */
	private Boolean checkAlwaysTaskAndEndEvent(List<FlowNode> nodes) {
		for (FlowNode node : nodes) {
			if ((node.getClass() != TaskImpl.class) && (node.getClass() != EndEventImpl.class)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Verifica se todos os nós da lista são somente de END Event
	 */
	private Boolean checkAlwaysEndEvent(List<FlowNode> nodes) {
		for (FlowNode node : nodes) {
			if (node.getClass() != EndEventImpl.class) {
				return false;
			}
		}
		return true;
	}

}
