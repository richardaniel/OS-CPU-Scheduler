package com.os.backend.Schedule;

import com.os.backend.Process.Process;
import com.os.backend.Process.ProcessExecutionEvent;
import com.os.backend.Process.ProcessState;
import com.os.backend.Process.ProcessTable;

import java.util.*;
import java.util.stream.Collectors;

public class SJF_Pree extends SchedulingAlgo {
    private List<Process> clonedProcesses;

    public static void main(String[] args) {
        // Create some sample processes
        Process p1 = new Process(1, 0, 5);
        Process p2 = new Process(2, 1, 3);
        Process p3 = new Process(3, 2, 2);
        Process p4 = new Process(4, 3, 5);

        // Add the processes to the SJF preemptive scheduling algorithm
        SJF_Pree sjfPree = new SJF_Pree();
        sjfPree.addNewProcesses(List.of(p1, p2, p3, p4));

        // Execute the SJF preemptive scheduling algorithm
        ProcessTable processTable = sjfPree.execute();

        // Output the execution events
        for (ProcessExecutionEvent event : processTable.getExecutionEvents()) {
            System.out.println(event);
        }
    }

    public SJF_Pree() {
    }

    @Override
    public ProcessTable execute() {
        ProcessTable processTable = new ProcessTable();
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(Comparator.comparingInt(Process::getRemainingTime).thenComparing(Process::getArrivalTime));
        int currentTime = 0;

        while (!clonedProcesses.isEmpty()) { // Continue until all processes are executed
            // Get the processes that have arrived by the current time
            List<Process> arrivedProcesses = getArrivedProcesses(currentTime);

            if (arrivedProcesses.isEmpty()) {
                // If no processes have arrived, increment current time
                currentTime++;
                continue;
            }

            // Add the arrived processes to the ready queue
            for (Process arrivedProcess : arrivedProcesses) {
                if (!readyQueue.contains(arrivedProcess)) {
                    readyQueue.add(arrivedProcess);
                }
            }

            // Get the process with the shortest burst time
            Process runningProcess = readyQueue.poll();


            // Ensure that a process is available for execution
            assert runningProcess != null;
            runningProcess.decrementRemainingTime();

            // Add event for process arrival
            processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.ARRIVED);

            // Add event for process start
            processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.STARTED);



            int burstTime = runningProcess.getRemainingTime();
            int endTime = 0;

            while(runningProcess.getRemainingTime() != 0){
                arrivedProcesses.clear();
                arrivedProcesses.addAll(getArrivedProcesses(++currentTime));

                // Add only the new arrived processes to the ready queue
                for (Process arrivedProcess : arrivedProcesses) {
                    if (!readyQueue.contains(arrivedProcess)) {
                        readyQueue.add(arrivedProcess);
                    }
                }

                if (!readyQueue.isEmpty()) {
                    Process nextProcess = readyQueue.peek();
                    if (nextProcess != null && nextProcess != runningProcess) {
                        // Process is interrupted
                        processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.INTERRUPTED);
                        assert readyQueue.peek() != null;
                        runningProcess = readyQueue.poll();
                        assert runningProcess != null;
                        runningProcess.decrementRemainingTime();
                        processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.ARRIVED);
                        processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.STARTED);
                    } else {
                        // Process continues running
                        processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.RUNNING);
                        runningProcess.decrementRemainingTime();
                    }
                }
            }

            processTable.addExecutionEvent(runningProcess, currentTime, runningProcess.getProcessNumber(), ProcessState.COMPLETED);

            // Remove the executed process from ready queue
            readyQueue.remove(runningProcess);
            // Remove the executed process from the list
            clonedProcesses.remove(runningProcess);
            currentTime++;
        }

        return processTable;
    }

    private List<Process> getArrivedProcesses(int currentTime) {
        List<Process> arrivedProcesses = new ArrayList<>();
        for (Process process : clonedProcesses) {
            if (process.getArrivalTime() <= currentTime) {
                arrivedProcesses.add(process);
            }
        }
        return arrivedProcesses;
    }

    @Override
    public void addNewProcesses(List<Process> newProcesses) {
        super.addNewProcesses(newProcesses);
        cloneProcessList();
    }

    // Helper methods
    private void cloneProcessList() {
        this.clonedProcesses = processesList.stream()
                .map(Process::clone)
                .collect(Collectors.toList());
    }

    @Override
    public String getSchedulerName() {
        return "Preemptive Shortest Job First";
    }
}