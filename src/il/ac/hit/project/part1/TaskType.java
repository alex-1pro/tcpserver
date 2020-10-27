package il.ac.hit.project.part1;

public enum TaskType {
    IO(1),
    COMPUTATIONAL(2),
    UNKNOWN(3);

    private int priority;

    private TaskType(int priority){
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        switch (priority){
            case 1 :
                return "IO";
            case 2:
                return "COMPUTATIONAL";
            case 3:
                return "UNKNOWN";
        };

        return "UNKNOWN";
    }
}
