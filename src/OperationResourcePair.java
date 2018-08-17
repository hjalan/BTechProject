public class OperationResourcePair {
    private Operation operation;
    private String resourceName;

    public int getOperationId() {
        return operation.getOperationId();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    @Override
    public String toString() {
        return operation.getOperationId() + "," + resourceName;
    }

}
