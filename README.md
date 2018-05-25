# BPMN Validator - BPMN Analysis from Process Discovery Contest @ BPM 2017 

This module has the purpose of validating a BPMN 2.0 modeling from a predetermined execution flow.

The BPMN (Business Process Model and Notation) notation specifies the business processes of an organization through a symbol grammar in a diagram. Because it is an agreed standard, an API is used to read the input and streaming models of activities and events, thus avoiding the rework of rewriting a standard already implemented in several APIs. Among the available APIs, the chosen one was the Camunda (version 7.8.0), the main motivator of the choice is the availability of an open source version. As a Java-based framework, Java has become the best candidate for the development of this validator module.

## Instalation

You must compile the solution to run it. To do this, the project was developed with a package manager and build, called Maven. With it available on the workstation and a version of JDK greater than 1.8.0 installed, just run the following command inside the project root folder that will be generated a java (.jar) executable.
```
	mvn package
```

## Premises
Two entries are expected, the first a file in the BPMN 2.0 standard (.bpmn extension) with the modeling that will then follow the stream specified by the second entry, an Excel file (.CSV extension) recognized here as a Log, columns that the Case and Event specifies respectively.
 
In the above example, you will validate for Case 1, the sequence of events in the reading order, lines 2, 3 and 4 events 'w', 'e' and 'm' and also Case 2, lines 5, 6 and 7 events 'w', 'c' and 's'.

As an output the validator presents a graphic screen showing the valid and invalid cases besides separating in files, in the same path of the last Log as input, adding to the file name: '_true' for the valid ones and '_false' for the invalid ones .

# Example

Suppose the following model (Graphical Representation).

To validate case 1, flow 'w', 'e', 'm'.


The first entry, that is, the BPMN template, is read by the Camunda API and disposed in its own object.
```java
    	var bpmn = require("bpmn");
	// We assume there is a myProcess.js besides myProcess.bpmn that contains the handlers
	bpmn.createUnmanagedProcess("path/to/myProcess.bpmn", function(err, myProcess){

        // we start the process
        myProcess.triggerEvent("MyStart");

	});
```    
 

The second entry, the Excel file, with the stream to be validated is read and stored in an object of type Log, created specifically for this.
```java
    public List<Log> readLogCsvFile(String path) {
		List<Log> logs = new ArrayList<Log>();

		try {
			File file = new File(path);
			InputStream fileStream = new FileInputStream(file);
			BufferedReader bufferFile = new BufferedReader(new InputStreamReader(fileStream));
			bufferFile.readLine(); //Pula o cabeçalho do arquivo
			String line = bufferFile.readLine();
			while (line != null) {
				Log log = new Log();
				log.setId(line.split(",")[0]);
				int index = logs.indexOf(log);
				if (index != -1) {
					logs.get(index).getEvents().add(line.split(",")[1]);
				} else {
					log.getEvents().add(line.split(",")[1]);
					logs.add(log);
				}
				line = bufferFile.readLine();
			}
			bufferFile.close();
		} catch (IOException e) {
			System.out.println("Erro na leitura do CSV.");
		}
		
		return logs;
	}
```

## Start Node

If both inputs conform to the assumptions and no exception is generated, it is time to go through the BPMN and validate the execution flow. To start the scan it is necessary to find Start Node, responsible for the beginning of the process.

```java
    FlowNode startNode = null;
		for (FlowNode flowNode : flowNodes) {
			if (flowNode.getClass() == StartEventImpl.class) {
				startNode = flowNode;
				break;
			}
		}
```

The search is performed from the objects provided by the API Camunda. Then from the list of nodes read from the input model, if any of these nodes are of type StartEvent.class then we have the starting point.

## Logic

Starting from the Start Event, algorithmic logic is applied as follows: The API makes the subsequent or previous nodes available to any node in its structure. Now that the starting point is defined, the algorithm works recursively looking for the nodes after each node found in the structure in order to match the sequence of events of the case being treated.
However, some of us titled by the framework are actually Gateways, Tasks or BPMN Notation Events, where each of them needs a specific treatment for changing the execution flow.
Each of these nodes generates a new recursive call with the list of nodes subsequent to it. If one of these recursive calls results in a single end node of End Event and the event list of the case being analyzed has already been fully covered, the case is considered valid.

## Tasks

They are the basis of comparison for the list of events to be validated. If the task is the same one that is at the top of the event list, the event is removed from the list and task generates a new recursive call with its subsequent nodes.

```java
    for(Iterator<FlowNode> node = currentNodes.iterator(); node.hasNext();) {
					FlowNode currentNode = node.next();
					
					if(currentNode.getName().equals(currentLog)) {
						System.out.println("> " + currentNode.getName());
						countRemoveNode++;
```

## Exclusive Gateway

It can only contain one correct output, ie the flow follows only one path.

```java
    if(currentNode.getClass() == ExclusiveGatewayImpl.class) {
        while(iterator.hasNext()) {
            SequenceFlow sequence = iterator.next();
            FlowNode newCurrentNode = sequence.getTarget();
```

To do this, each output is treated after the exclusive gateway so that the current state of the nodes is saved and the flow follows the first output found, if it is correct, ie associated with the current event of the case being validated, the normal. Otherwise the execution is returned and the next output is evaluated in the same way. If none of the outputs relate to the stream being validated, the analyzed case is an invalid one.

```java
        List<FlowNode> newCurrentNodesCopy = new ArrayList<>(newCurrentNodes);
							
            if (!newCurrentNodes.contains(newCurrentNode)) {
                newCurrentNodes.add(newCurrentNode);
            }
            
            for(int i = (currentNodes.indexOf(currentNode) + 1); i < currentNodes.size(); i++) {
                if(!newCurrentNodes.contains(currentNodes.get(i))) {
                    newCurrentNodes.add(currentNodes.get(i));
                }
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
```

## Parallel Gateway

In this case, all outputs form a parallel execution. All subsequent nodes are evaluated and they necessarily need to be the next events of the case being validated.

```java
    if(currentNode.getClass() == ParallelGatewayImpl.class) {
        if(isActiveNode(currentNode, currentNodes)) {
            while (iterator.hasNext()) {
                SequenceFlow sequence = iterator.next();
                FlowNode newCurrentNode = sequence.getTarget();
                if (!newCurrentNodes.contains(newCurrentNode)) {
                    newCurrentNodes.add(newCurrentNode);
                }
            }
        } 
    }
```

## Inclusive Gateway

For this case, where it can contain only valid output such as all subsequent nodes are valid, a combination rule is applied to the outputs in order to test any and all available possibilities.

```java
    if(currentNode.getClass() == InclusiveGatewayImpl.class) {
        if(isActiveNode(currentNode, currentNodes)) {
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
```

It basically saves the current state of the nodes and executes every possible combination of the later nodes, if none of these combinations results in the expected sequence of events, the case is invalidated.

```java
     while ( comb.hasNext() ) {
            List<FlowNode> listCombInclusiveNodes = new ArrayList<>();
            listCombInclusiveNodes = comb.next() ;
            
            List<FlowNode> newCurrentNodesCopy = new ArrayList<>(newCurrentNodes);
                
            for ( FlowNode e : listCombInclusiveNodes ) {
                newCurrentNodes.add(e);
            }
            
            for(int i = (currentNodes.indexOf(currentNode) + 1); i < currentNodes.size(); i++) {
                    if(!newCurrentNodes.contains(currentNodes.get(i))) {
                    newCurrentNodes.add(currentNodes.get(i));
                }
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
    }
```

## End Node

Final Event, where if the list of events being analyzed has already been completely covered, the case is considered valid.

# BPMN - Supported Elements

Start Event - starts the execution flow.
End Event - End event ends execution flow.
Gateways - Exclusive, Parallel and Inclusive are supported.
Tasks - Tasks are supported.

## Limitations

In the course of the implementation a problem was presented on the Parallel nodes as well as in cases where the combination of nodes subsequent to the Inclusive Gateway is greater than one. In these cases, where there are parallel outputs to a node, it is expected that the flow of these two or more outputs will eventually meet again (in the latter case, this union will occur for the final event). However, until this re-encounter, flows can have different sizes, where one of the flows can reach the rendezvous point first, when arriving at the rendezvous point first one must wait until the other (more than one) flow reaches this point same point to give continuity in the execution of the process.
In order to solve these problems, a concept of active node was created and implemented the following solution: when a node has more than one predecessor node to it, ie is a meeting point of flows, it is verified if its predecessors and consequently the precedents (2-level verification only) are active nodes. To be an active node, this check can not return any node that is still in the event list of the case being analyzed, that is, if any predecessor node to the flow encounter is still in the list of events to be verified, it is because the flows have different sizes and you have to wait. However the limitation is to be verified in only two predecessor levels, because there are cases with more than two levels, where an infinite loop is generated.

# Applied Case Study - Modelos e Logs do "Process Discovery Contest @ BPM 2017"

A total of 10 BPMN models (numbered from 1 to 10) and 10 Log files (numbered from 1 to 10) were made available, a model for their respective Log file. Where each Log contains 20 cases to be analyzed. Among these 10 logs, 4 of them were analyzed manually, being 2, 3, 6 and 7, in order to compare the results and prove the efficiency of the tool. For those cases analyzed manually the tool presented 100% correctness. Follow the results of the 10 models.  

Model 1) 10 Valid 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/1.PNG)

Model 2) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/2.PNG)

Model 3) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/3.PNG)

Model 4) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/4.PNG)

Model 5) 7 Valid e 13 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/5.PNG)

Model 6) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/6.PNG)

Model 7) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/7.PNG)

Model 8) 10 Valid e 10 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/8.PNG)

Model 9) 6 Valid e 14 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/9.PNG)

Model 10) 6 Valid e 14 Invalid

![alt text](https://github.com/isabelinda/isabelavalonni_bpmn_validator/tree/master/Executavel%20e%20documentacao/Resultados/10.PNG)

However, because it was a controlled study, even without knowing which cases, it was already known that each Log file should have 10 valid and 10 invalid cases, which was not the case for models 5, 9 and 10 due to the mentioned limitations.


## Licensing 
(The MIT License)

Copyright (c) 2018 Isabela Valonni

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

- Include the contributer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

by Isabela Valonni