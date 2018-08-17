import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TextReader {
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private List<Operation> operationList;
    private int maxNumAdders;
    private int maxNumMultipliers;

    public int getMaxNumAdders() {
        return maxNumAdders;
    }

    public void setMaxNumAdders(int maxNumAdders) {
        this.maxNumAdders = maxNumAdders;
    }

    public int getMaxNumMultipliers() {
        return maxNumMultipliers;
    }

    public void setMaxNumMultipliers(int maxNumMultipliers) {
        this.maxNumMultipliers = maxNumMultipliers;
    }

    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }

    List<String> readSmallTextFile(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        return Files.readAllLines(path, ENCODING);
    }

    public void buildOperationList(String[] strings) {
        int numAdders = 0;
        int numMultipliers = 0;
        for (int i = 0; i < strings.length; i++) {
            char sign = '!';
            if (strings[i].equals("+") || strings[i].equals("-")) {
                sign = '+';
                numAdders++;
            } else if (strings[i].equals("*")) {
                sign = '*';
                numMultipliers++;
            }
            if (sign != '!') {
                Operation operation = new Operation();
                operation.setSign(sign);
                operation.setInput1(Integer.parseInt(strings[i + 1]));
                operation.setInput2(Integer.parseInt(strings[i + 2]));
                operation.setOperationId(Integer.parseInt(strings[i + 3]));
                operationList.add(operation);
            }
        }
        if (numAdders > maxNumAdders)
            maxNumAdders = numAdders;
        if (numMultipliers > maxNumMultipliers)
            maxNumMultipliers = numMultipliers;
    }
}
