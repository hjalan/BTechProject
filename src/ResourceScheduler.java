import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ResourceScheduler {
    private List<Resource> resourceList;
    private int numAdders;
    private int numMultipliers;
    private int maxNumAdders;
    private int maxNumMultipliers;
    private int numRegisters;
    private final static int kC = 1;
    private final static int entropyThreshold = 3;
    final static String FILE_NAME = "C:\\Users\\Harshit Jalan\\Desktop\\BTP\\DFG\\test.txt";
    final static String OUTPUT_FILE_NAME = "C:\\Users\\Harshit Jalan\\Desktop\\BTP\\test_Schedule.txt";

    private List<Operation> operationList;
    private Map<Integer, Boolean> operationIdToStatusMap;

    private Integer clockCycle = 1;
    private int numOperationsScheduled;
    private List<OperationResourcePair> scheduledOperationList;
    private Map<Integer, Boolean> tempOperations;
    private List<List<Integer>> operationInputList;
    private List<List<Integer>> operationOutputList;
    private List<List<Integer>> registerAllocation;
    private List<List<Integer>> newRegisterAllocation;
    private int numInputs;
    private Map<Integer, Integer> operationToRegisterMap;
    private int inputCount;
    private int entropy;

    private void buildResourceList() {
        resourceList = new ArrayList<>();
        for (int i = 0; i < numAdders; i++) {
            Resource resource = new Resource();
            resource.setSign('+');
            resource.setUsableAfter(0);
            resource.setResourceName("A" + Integer.toString(i + 1));
            resourceList.add(resource);
        }
        for (int i = 0; i < numMultipliers; i++) {
            Resource resource = new Resource();
            resource.setSign('*');
            resource.setUsableAfter(0);
            resource.setResourceName("M" + Integer.toString(i + 1));
            resourceList.add(resource);
        }
    }

    private void nextClockCycle() {
        clockCycle++;
        for (Resource resource : resourceList) {
            int usableAfter = resource.getUsableAfter();
            if (usableAfter > 0)
                resource.setUsableAfter(usableAfter - 1);
        }
    }

    private boolean isOperationReady(@NotNull Operation operation) {
        Boolean isInput1Available = operationIdToStatusMap.get(operation.getInput1());
        Boolean isInput2Available = operationIdToStatusMap.get(operation.getInput2());
        Boolean isSameClockCycle1 = tempOperations.get(operation.getInput1());
        Boolean isSameClockCycle2 = tempOperations.get(operation.getInput2());
        if (operation.getInput1() == 0 && operation.getInput2() == 0) {
            return true;
        } else if (operation.getInput1() == 0) {
            if (isInput2Available != null && isInput2Available.equals(Boolean.TRUE) && isSameClockCycle2 == null) {
                return true;
            }
        } else if (operation.getInput2() == 0) {
            if (isInput1Available != null && isInput1Available.equals(Boolean.TRUE) && isSameClockCycle1 == null)
                return true;
        } else if (isInput1Available != null && isInput2Available != null && isInput1Available.equals(Boolean.TRUE) && isInput2Available.equals(Boolean.TRUE)
                && isSameClockCycle1 == null && isSameClockCycle2 == null) {
            return true;
        }
        return false;
    }

    private void scheduleForOneClockCycle() throws IOException {
        scheduledOperationList = new LinkedList<>();
        tempOperations = new HashMap<>();
        tempOperations.put(0, Boolean.TRUE);
        for (Operation operation : operationList) {
            Boolean isOperationComplete = operationIdToStatusMap.get(operation.getOperationId());
            if (isOperationComplete != null && isOperationComplete.equals(Boolean.TRUE)) {
                continue;
            }
            if (isOperationReady(operation)) {
                for (Resource resource : resourceList) {
                    if (resource.getUsableAfter() == 0 && resource.getSign() == operation.getSign()) {
                        resource.setUsableAfter(kC);
                        operationIdToStatusMap.put(operation.getOperationId(), Boolean.TRUE);
                        numOperationsScheduled++;
                        tempOperations.put(operation.getOperationId(), Boolean.TRUE);
                        OperationResourcePair operationResourcePair = new OperationResourcePair();
                        operationResourcePair.setResourceName(resource.getResourceName());
                        operationResourcePair.setOperation(operation);
                        scheduledOperationList.add(operationResourcePair);
                        break;
                    }
                }
            }
        }
    }

    private void calcNumRegisters() {
        List<Integer> outputList = new ArrayList<>();
        List<Integer> inputList = new ArrayList<>();
        for (OperationResourcePair operationResourcePair : scheduledOperationList) {

            outputList.add(operationResourcePair.getOperationId());
            int input1 = operationResourcePair.getOperation().getInput1();
            int input2 = operationResourcePair.getOperation().getInput2();

            if (input1 == input2 && input1 != 0) {
                inputList.add(input1);
            } else {
                if (input1 != 0 && input2 != 0) {
                    inputList.add(input1);
                    inputList.add(input2);
                } else if (input1 != 0) {
                    inputList.add(input1);
                } else if (input2 != 0) {
                    inputList.add(input2);
                }
            }
        }

        int extraOutputs = outputList.size() - inputList.size();
        if (extraOutputs > 0)
            numRegisters += extraOutputs;
        operationOutputList.add(outputList);
        operationInputList.add(inputList);
    }

    private void allocateRegisters() {

        List<Integer> newAllocation = new ArrayList<>();
        newAllocation.addAll(registerAllocation.get(clockCycle - 1));

        for (OperationResourcePair operationResourcePair : scheduledOperationList) {

            int input1 = operationResourcePair.getOperation().getInput1();
            int input2 = operationResourcePair.getOperation().getInput2();

            if (input1 == input2 && input1 != 0) {
                int inputRegister = newAllocation.indexOf(operationToRegisterMap.get(input1));
                newAllocation.set(inputRegister, numInputs);
                operationToRegisterMap.put(operationResourcePair.getOperationId(), numInputs);
                numInputs++;
            } else if (input1 == input2 && input1 == 0) {
                newAllocation.set(inputCount++, numInputs);
                operationToRegisterMap.put(operationResourcePair.getOperationId(), numInputs);
                numInputs++;
            } else {
                if (input1 != 0 && input2 != 0) {
                    int input1Register = newAllocation.indexOf(operationToRegisterMap.get(input1));
                    int input2Register = newAllocation.indexOf(operationToRegisterMap.get(input2));

                    newAllocation.set(input1Register < input2Register ? input1Register : input2Register, numInputs);
                    newAllocation.set(input1Register > input2Register ? input1Register : input2Register, -1);
                    operationToRegisterMap.put(operationResourcePair.getOperationId(), numInputs);
                    numInputs++;
                } else if (input1 != 0) {
                    int input1Register = newAllocation.indexOf(operationToRegisterMap.get(input1));
                    newAllocation.set(input1Register, numInputs);
                    operationToRegisterMap.put(operationResourcePair.getOperationId(), numInputs);
                    newAllocation.set(inputCount++, -1);
                    numInputs++;
                } else if (input2 != 0) {
                    int input2Register = newAllocation.indexOf(operationToRegisterMap.get(input2));
                    newAllocation.set(input2Register, numInputs);
                    operationToRegisterMap.put(operationResourcePair.getOperationId(), numInputs);
                    newAllocation.set(inputCount++, -1);
                    numInputs++;
                }
            }
        }
        registerAllocation.add(newAllocation);
    }

    private void scheduleAndWrite() throws IOException {
        numOperationsScheduled = 0;
        numRegisters = 0;
        operationIdToStatusMap = new HashMap<>();
        operationInputList = new ArrayList<>();
        operationOutputList = new ArrayList<>();
        operationIdToStatusMap.put(0, Boolean.TRUE);
        operationToRegisterMap = new HashMap<>();
        inputCount = 0;
        while (numOperationsScheduled < operationList.size()) {
            scheduleForOneClockCycle();
            writeScheduledOperationsToFile();
            calcNumRegisters();
            allocateRegisters();
            nextClockCycle();
        }
        TextWriter textWriter = new TextWriter();
        List<String> outputStringList = new ArrayList<>();
        outputStringList.add("Latency = " + (clockCycle - 1) * 1000 + "ps");
        outputStringList.add("Number of Registers Required = " + numRegisters);
        outputStringList.add("Register Allocation :");
        registerAllocation.forEach(e -> outputStringList.add(e.toString()));
        textWriter.writeSmallTextFile(outputStringList, OUTPUT_FILE_NAME);

        for (int i = 0; i < clockCycle - 1; i++)
            System.out.println("inputs " + operationInputList.get(i) + " outputs " + operationOutputList.get(i));
        System.out.println(numInputs);
    }

    private void calcNumInputs() {
        numInputs = 0;
        registerAllocation = new ArrayList<>();
        List<Integer> inputRegisters = new ArrayList<>();
        for (Operation operation : operationList) {
            if (operation.getInput1() == 0 || operation.getInput2() == 0) {
                inputRegisters.add(numInputs);
                numInputs++;
            }
        }
        registerAllocation.add(inputRegisters);
    }

    private int searchOverlaps(int cc, int registerA, int registerB, int next, List<List<Integer>> copyRegisterAllocation, int loc) {
        int entropy = 0;

        while ((cc + next) >= 0 && (cc + next) < copyRegisterAllocation.size() && cc != loc) {
            if (copyRegisterAllocation.get(cc + next).get(registerA) == copyRegisterAllocation.get(cc).get(registerA)) {
                if (copyRegisterAllocation.get(cc + next).get(registerB) != -1 && copyRegisterAllocation.get(cc + next).get(registerB) != copyRegisterAllocation.get(cc).get(registerB))
                    entropy++;
                int temp = copyRegisterAllocation.get(cc).get(registerB);
                copyRegisterAllocation.get(cc).set(registerB, copyRegisterAllocation.get(cc).get(registerA));
                copyRegisterAllocation.get(cc).set(registerA, temp);
                cc += next;
                if (cc == 0 || cc == copyRegisterAllocation.size() - 1) {
                    temp = copyRegisterAllocation.get(cc).get(registerB);
                    copyRegisterAllocation.get(cc).set(registerB, copyRegisterAllocation.get(cc).get(registerA));
                    copyRegisterAllocation.get(cc).set(registerA, temp);
                }
            } else {
                if (copyRegisterAllocation.get(cc + next).get(registerB) == copyRegisterAllocation.get(cc).get(registerB)) {
                    int temp = registerA;
                    registerA = registerB;
                    registerB = temp;
                    entropy++;
                    temp = copyRegisterAllocation.get(cc).get(registerB);
                    copyRegisterAllocation.get(cc).set(registerB, copyRegisterAllocation.get(cc).get(registerA));
                    copyRegisterAllocation.get(cc).set(registerA, temp);
                    cc += next;
                    if (cc == 0 || cc == copyRegisterAllocation.size() - 1) {
                        temp = copyRegisterAllocation.get(cc).get(registerB);
                        copyRegisterAllocation.get(cc).set(registerB, copyRegisterAllocation.get(cc).get(registerA));
                        copyRegisterAllocation.get(cc).set(registerA, temp);
                    }
                } else {
                    int temp = copyRegisterAllocation.get(cc).get(registerB);
                    copyRegisterAllocation.get(cc).set(registerB, copyRegisterAllocation.get(cc).get(registerA));
                    copyRegisterAllocation.get(cc).set(registerA, temp);
                    return entropy;
                }
            }
        }
        if (cc == loc)
            entropy = 1;
        return entropy;
    }

    private int addEdge(int ccA, int ccB, int registerColor)throws IOException {
        int maxEntropy = 0;
        for (int i = 0; i < numRegisters; i++) {
            if (i == registerColor)
                continue;
            if (registerAllocation.get(ccA).get(i) == -1)
                entropy = 1;
            else if (registerAllocation.get(ccA).get(i) != -1)
                entropy = 2;
            List<List<Integer>> copyRegisterAllocation = new ArrayList<>();
            registerAllocation.forEach(e -> {
                List<Integer> list = new ArrayList<>();
                list.addAll(e);
                copyRegisterAllocation.add(list);
            });
            entropy += searchOverlaps(ccA, registerColor, i, 1, copyRegisterAllocation, ccB);
            List<List<Integer>> copyRegisterAllocation2 = new ArrayList<>();
            registerAllocation.forEach(e -> {
                List<Integer> list = new ArrayList<>();
                list.addAll(e);
                copyRegisterAllocation2.add(list);
            });
            entropy += searchOverlaps(ccA, registerColor, i, -1, copyRegisterAllocation2, ccB);
            if (entropy > maxEntropy)
                maxEntropy = entropy;
            List<String> outputStringList = new ArrayList<>();
            outputStringList.add("Edge between V" + registerAllocation.get(ccA).get(registerColor) + " and V" + registerAllocation.get(ccB).get(registerColor) + " register " + (i + 1) + " has entropy " + entropy);
            for (int j = 0; j < ccA; j++)
                outputStringList.add(copyRegisterAllocation2.get(j).toString());
            for (int j = ccA; j < copyRegisterAllocation.size(); j++)
                outputStringList.add(copyRegisterAllocation.get(j).toString());
            if (registerAllocation.get(ccB).get(i) == -1)
                entropy = 1;
            else if (registerAllocation.get(ccB).get(i) != -1)
                entropy = 2;
            List<List<Integer>> copyRegisterAllocation3 = new ArrayList<>();
            registerAllocation.forEach(e -> {
                List<Integer> list = new ArrayList<>();
                list.addAll(e);
                copyRegisterAllocation3.add(list);
            });
            entropy += searchOverlaps(ccB, registerColor, i, 1, copyRegisterAllocation3, ccA);
            List<List<Integer>> copyRegisterAllocation4 = new ArrayList<>();
            registerAllocation.forEach(e -> {
                List<Integer> list = new ArrayList<>();
                list.addAll(e);
                copyRegisterAllocation4.add(list);
            });
            entropy += searchOverlaps(ccB, registerColor, i, -1, copyRegisterAllocation4, ccA);
            if (entropy > maxEntropy)
                maxEntropy = entropy;
            outputStringList.add("Edge between V" + registerAllocation.get(ccA).get(registerColor) + " and V" + registerAllocation.get(ccB).get(registerColor) + " register " + (i + 1) + " has entropy " + entropy);
            for (int j = 0; j < ccA; j++)
                outputStringList.add(copyRegisterAllocation4.get(j).toString());
            for (int j = ccA; j < copyRegisterAllocation3.size(); j++)
                outputStringList.add(copyRegisterAllocation3.get(j).toString());

            for (int j = 0; j < ccB; j++)
                newRegisterAllocation.add(copyRegisterAllocation4.get(j));
            for (int j = ccB; j < copyRegisterAllocation3.size(); j++)
                newRegisterAllocation.add(copyRegisterAllocation3.get(j));

            TextWriter textWriter = new TextWriter();
            textWriter.writeSmallTextFile(outputStringList, OUTPUT_FILE_NAME);
        }
        return maxEntropy;
    }

    private void calcEntropy() throws IOException {
        for (int registerColor = 0; registerColor < numRegisters; registerColor++) {
            for (int ccA = 0; ccA < clockCycle; ccA++) {
                if (ccA != 0 && registerAllocation.get(ccA).get(registerColor) == registerAllocation.get(ccA - 1).get(registerColor))
                    continue;
                for (int ccB = ccA + 1; ccB < clockCycle; ccB++) {
                    if (registerAllocation.get(ccB).get(registerColor) == registerAllocation.get(ccB - 1).get(registerColor))
                        continue;
                    newRegisterAllocation = new ArrayList<>();
                    if (registerAllocation.get(ccA).get(registerColor) != registerAllocation.get(ccB).get(registerColor)
                            && (registerAllocation.get(ccA).get(registerColor) != -1 && registerAllocation.get(ccB).get(registerColor) != -1))
                        if (addEdge(ccA, ccB, registerColor) < entropyThreshold) {
                            System.out.println("Edge between V" + registerAllocation.get(ccA).get(registerColor) + " and V" + registerAllocation.get(ccB).get(registerColor) + " can be added");
                            System.out.println("New register allocation)");
                            newRegisterAllocation.forEach(e -> System.out.println(e));
                        }
                }
            }
        }
    }

    private void writeScheduledOperationsToFile() throws IOException {
        StringBuilder outputString = new StringBuilder();
        outputString.append(clockCycle.toString() + "--");
        for (OperationResourcePair operationResourcePair : scheduledOperationList) {
            outputString.append(operationResourcePair.toString() + " ");
        }
        TextWriter textWriter = new TextWriter();
        List<String> outputStringList = new ArrayList<>();
        outputStringList.add(outputString.toString());
        textWriter.writeSmallTextFile(outputStringList, OUTPUT_FILE_NAME);
    }

    private void readOperationDataFromBenchmark() throws IOException {
        TextReader textReader = new TextReader();
        List<String> stringList = textReader.readSmallTextFile(FILE_NAME);
        textReader.setOperationList(new ArrayList<>());
        textReader.setMaxNumAdders(0);
        textReader.setMaxNumMultipliers(0);
        stringList.forEach(e -> textReader.buildOperationList(e.split(",")));
        operationList = textReader.getOperationList();
        maxNumAdders = textReader.getMaxNumAdders();
        maxNumMultipliers = textReader.getMaxNumMultipliers();

        TextWriter textWriter = new TextWriter();
        stringList.add(0, "Benchmark data:");
        stringList.add("");
        stringList.add("Scheduled Operations");
        textWriter.writeSmallTextFile(stringList, OUTPUT_FILE_NAME);
    }

    public static void main(String args[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ResourceScheduler resourceScheduler = new ResourceScheduler();
        resourceScheduler.readOperationDataFromBenchmark();
        System.out.println("Enter number of Adders to use (1 - " + resourceScheduler.maxNumAdders + ")");
        resourceScheduler.numAdders = Integer.parseInt(br.readLine());
        System.out.println("Enter number of Multipliers to use (1 - " + resourceScheduler.maxNumMultipliers + ")");
        resourceScheduler.numMultipliers = Integer.parseInt(br.readLine());
        resourceScheduler.calcNumInputs();
        resourceScheduler.buildResourceList();
        resourceScheduler.scheduleAndWrite();
        resourceScheduler.calcEntropy();
    }
}
