package il.ac.hit.project.algorithms;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

// contains implementations for methods solving the 4 tasks specified
public class Algorithms {
    // === task 1 ===
    // receives a matrix containing 0's and 1's
    // returns a list of the connected components in the matrix (each connected component represented by a HashSet<Index>)
    // throws AlgorithmThreadingException if a threading-related exception occurred during execution
    public static List<HashSet<Index>> findAllConnectedComponents(int[][] matrix) throws AlgorithmThreadingException {
        // a map where each index the matrix is mapped to its connected component
        Map<Index, HashSet<Index>> indexToConnectedHashSetMap = new HashMap<>();
        // lock  2 threads won't enter to critical section
        ReentrantReadWriteLock connectedHashSetMatrixLock = new ReentrantReadWriteLock();

        // one thread for each crawl start position
        ExecutorService executorService = Executors.newFixedThreadPool(matrix.length * matrix[0].length);

        // initialize all indices to point to null (marks them as unvisited)
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                indexToConnectedHashSetMap.put(new Index(i, j), null);
            }
        }

        // for each (i,j) in matrix start crawling at index (i,j)
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                Index newIndex = new Index(i, j);
                executorService.execute(
                        () -> crawlAtLocation(newIndex,
                                null, indexToConnectedHashSetMap, matrix, connectedHashSetMatrixLock));
            }
        }

        shutdownAndWaitForTasksToFinish(executorService);

        // remove nulls and duplicates from values(), collect into a list of HashSet<Index>
        return indexToConnectedHashSetMap.values().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    // === task 2 ===
    // receives a matrix containing 0's and 1's, index of the source index, index of the destination index
    // returns a list containing all paths from source index to destination index in the matrix
    // (each path is represented by a list of indices)
    // throws AlgorithmThreadingException if a threading-related exception occurred during execution
    public static List<List<Index>> findAllPathsFromSourceToDest(int[][] matrix, Index source, Index dest)
            throws AlgorithmThreadingException {
        // creates new threads as needed, but will reuse previously constructed threads when they are available
        // threads are disposed after 60 seconds of inactivity
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<List<Index>> paths = findPathFromCurrentIndex(source, new ArrayList<>(), dest, matrix, executorService);
        shutdownAndWaitForTasksToFinish(executorService);

        // sort paths by length (shortest path first)
        paths.sort(Comparator.comparingInt(List::size));
        return paths;
    }

    // === task 3 ===
    // receives a matrix containing 0's and 1's, index of the source index, index of the destination index
    // returns a list containing only the shortest paths from source index to destination index in the matrix
    // (each path is represented by a list of indices)
    public static List<List<Index>> findShortestPathsFromSourceToDest(int[][] matrix, Index source, Index dest) {
        // the length of the shortest path up until now
        // we put it in an array so it can be changed from any recursive call, and all other recursive calls can see
        // the change
        int[] shortestPathLength = {Integer.MAX_VALUE};
        List<List<Index>> shortestPaths = new ArrayList<>();

        findShortestPathFromCurrentIndex(source, new ArrayList<>(), dest, matrix, shortestPaths, shortestPathLength);

        // return a list containing only the paths with lengths smaller than or equal to shortest path length
        return shortestPaths.stream().filter((l) -> l.size() <= shortestPathLength[0]).collect(Collectors.toList());
    }

    // === task 4 ===
    // receives a matrix containing 0's and 1's representing a submarine game (each block of 1's represents a submarine)
    // returns the number of submarines in the game matrix
    //    // throws AlgorithmThreadingException if a threading-related exception occurred during execution
    // throws InvalidSubmarineMatrixAlgorithmException if game matrix is invalid, that is, an invalid submarine exists
    public static int countSubmarines(int[][] submarineGameMatrix)
            throws AlgorithmThreadingException, InvalidSubmarineMatrixAlgorithmException {
        // first find the connected components using method from task 1
        List<HashSet<Index>> allConnectedComponents = findAllConnectedComponents(submarineGameMatrix);

        // will store the validity result for each connected component (updated and read in a synchronized manner)
        List<Boolean> componentResults = new ArrayList<>();

        // check that each connected component is a rectangle
        // one thread for each connected component
        ExecutorService executorService = Executors.newFixedThreadPool(allConnectedComponents.size());

        for (HashSet<Index> connectedComponent : allConnectedComponents) {
            executorService.execute(
                    () -> {
                        boolean res = isValidSubmarine(connectedComponent);

                        // add the result to the componentResults list
                        // and notify the main thread that there is a new result waiting
                        synchronized (submarineGameMatrix) {
                            componentResults.add(res);
                            submarineGameMatrix.notify();
                        }
                    });
        }

        // check that each connected component represents a valid submarine
        // componentResults is updated in a synchronized manner (on the game matrix object) by each checker thread
        // and this thread runs through the results
        // if there exists an invalid submarine, InvalidSubmarineMatrixAlgorithmException is thrown

        // number of component validity results checked in total - will be same as number of connected components
        // if end of method is reached
        int resCount = 0;

        synchronized (submarineGameMatrix) {
            while (true) {
                // run through available results: if a result is false, throw InvalidSubmarineMatrixAlgorithmException
                for (boolean result : componentResults) {
                    ++resCount; // increment number of results checked

                    if (!result) {
                        // stop all other tasks as there's no need to continue checking the other components
                        executorService.shutdownNow();
                        throw new InvalidSubmarineMatrixAlgorithmException();
                    }
                }

                // clear result list
                componentResults.clear();

                if (resCount == allConnectedComponents.size()) { // results for all connected components checked - terminate loop
                    break;
                } else {
                    try {
                        // wait until a checker thread notifies that there's an additional result available
                        // note tha wait() releases the lock on submarineGameMatrix (so the checker threads can
                        // access their synchronized block and add the result to componentResults)
                        submarineGameMatrix.wait();
                    } catch (InterruptedException e) {
                        throw new AlgorithmThreadingException(e.getMessage());
                    }
                }
            }
        }

        // if we got here all connected components are rectangles, so matrix is valid

        // got results for all components so shutdown the executor service
        shutdownAndWaitForTasksToFinish(executorService);

        return allConnectedComponents.size();
    }


    // helper method for task 4
    // receives a matrix connected component, represented by a HashSet of indices
    // returns true if component represents a valid submarine, else false
    private static boolean isValidSubmarine(HashSet<Index> connectedComponent) {
        // first check that component's size is larger than 1
        if (connectedComponent.size() <= 1) {
            return false;
        }

        // next check that component is a rectangle

        // first find corners of bounding rectangle
        int left = Collections.min(connectedComponent, Comparator.comparingInt(Index::getCol)).getCol();
        int right = Collections.max(connectedComponent, Comparator.comparingInt(Index::getCol)).getCol();
        int top = Collections.min(connectedComponent, Comparator.comparingInt(Index::getRow)).getRow();
        int bottom = Collections.max(connectedComponent, Comparator.comparingInt(Index::getRow)).getRow();

        // check if the bounding rectangle is "filled", that is the indices in the connected component
        // form the bounding rectangle. if this is the case, then connected component is a rectangle
        for (int i = top; i <= bottom; i++) {
            for (int j = left; j <= right; j++) {
                Index index = new Index(i, j);

                if (!connectedComponent.contains(index)) {
                    return false;
                }
            }
        }

        // bounding rectangle filled by indices in connected component
        return true;
    }

    // recursive helper method for task 3
    //
    // receives:
    // index to start search from,
    // current path up until this point represented by a list of indices,
    // the index of the destination,
    // a matrix containing 0's and 1's,
    // the shortest paths list(a list containing lists of indices representing paths),
    // an array containing the currently shortest path length at (in shortestPathLength[0]).
    //
    // if index is the destination, and current path up to destination is smaller than or equal to current shortest path,
    // adds path to shortestPaths.
    // else, if index is not destination, but is a valid unvisited index, continues searching and building a path to destination
    // by recursively calling itself in all 8 possible directions
    private static void findShortestPathFromCurrentIndex(Index index, List<Index> currentPath, Index dest,
                                                         int[][] matrix, List<List<Index>> shortestPaths, int[] shortestPathLength) {
        // invalid index, or index already visited, or 0 in matrix at index
        if (!index.isValid(matrix.length, matrix[0].length) || currentPath.contains(index)
                || matrix[index.getRow()][index.getCol()] == 0) {
            return;
        }

        // add current index to path
        currentPath.add(index);

        // current path is already longer than shortest path, so no need to keep looking in this path
        if (currentPath.size() > shortestPathLength[0]) {
            return;
        }

        if (index.equals(dest)) { // reached destination - path found - add only current path to paths list
            shortestPaths.add(currentPath);

            // update current shortest path length if current path is shorter
            if (currentPath.size() < shortestPathLength[0]) {
                shortestPathLength[0] = currentPath.size();
            }
        } else { // destination not reached - keep looking in all directions
            // try going in all 8 directions
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // same as current index
                    Index nextIndex = new Index(index.getRow() + i, index.getCol() + j);

                    // make a recursive call - second argument is copy of current path
                    // so each recursive call has its on path list
                    findShortestPathFromCurrentIndex(nextIndex, new ArrayList<>(currentPath), dest, matrix,
                            shortestPaths, shortestPathLength);
                }
            }
        }
    }

    // recursive helper method for task 2
    //
    // receives:
    // index to start search from,
    // current path up until this point represented by a list of indices,
    // index of the destination,
    // a matrix containing 0's and 1's,
    // the executor service.
    //
    // returns a list of paths from current index to destination (each path represented by a list of indices)
    // if index is destination, returns a list containing a single path, containing only the destination.
    // else, if index is invalid or was already visited, returns an empty list.
    // else, continues searching and building a path to destination by recursively calling itself in all 8 possible directions
    // (each recursive call is submitted as a task to the executorService).
    // if one or more of these searches yields a path to destination, returns a list containing those paths,
    // and otherwise, returns an empty list
    // throws AlgorithmThreadingException if a threading-related exception occurred during execution
    private static List<List<Index>> findPathFromCurrentIndex(Index index, List<Index> currentPath, Index dest,
                                                              int[][] matrix, ExecutorService executorService)
            throws AlgorithmThreadingException {
        // invalid index, or index already visited, or 0 in matrix at index - return empty path list
        if (!index.isValid(matrix.length, matrix[0].length) || currentPath.contains(index)
                || matrix[index.getRow()][index.getCol()] == 0) {
            return new ArrayList<>();
        }

        // initialize an empty list of paths from current index
        ArrayList<List<Index>> pathsFromCurrent = new ArrayList<>();

        // add current index to path
        currentPath.add(index);

        if (index.equals(dest)) { // reached destination - path found - add only current path to paths list
            pathsFromCurrent.add(currentPath);
        } else {
            List<Future<List<List<Index>>>> futures = new ArrayList<>();

            // try going in all 8 directions
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // same as current index
                    Index nextIndex = new Index(index.getRow() + i, index.getCol() + j);
                    futures.add(executorService.submit(
                            // make a recursive call - second argument is copy of current path
                            // so each recursive call has its on path list
                            () -> findPathFromCurrentIndex(nextIndex,
                                    new ArrayList<>(currentPath),
                                    dest,
                                    matrix,
                                    executorService)));
                }
            }

            // get results from futures - these are the list of paths from the recursive calls
            for (Future<List<List<Index>>> future : futures) {
                try {
                    // future.get() will block if task is not completed
                    // but by this point all tasks (path lookups) from current were already submitted,
                    // so current thread should wait in case a task is not yet finished
                    pathsFromCurrent.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new AlgorithmThreadingException(e.getMessage());
                }
            }
        }

        return pathsFromCurrent;
    }

    // helper method for task 1
    // receives:
    // index of current index,
    // index of previous index crawled,
    // map containing a mapping from each index in the matrix to its current connected component (represented by a HashSet
    // of indices),
    // matrix containing 0's and 1's
    // a ReentrantReadWriteLock.
    //
    // if index is valid, and matrix contains a 1 at its location:
    // if index was not yet visited, tries adding index to the current connected
    // component being built, and continue crawling to all 8 directions find other indices in the connected component.
    // else, if the index belongs to a different connected component, merges the two connected components. this is done
    // by adding all indices in the two components to one of them, and remapping all indices to the chosen component
    // in the indexToConnectedHashSetMap.
    private static void crawlAtLocation(Index index, Index prevIndex, Map<Index, HashSet<Index>> indexToConnectedHashSetMap,
                                        int[][] matrix, ReentrantReadWriteLock connectedHashSetMatrixLock) {

        // check for illegal index (out of matrix bounds) or 0 in matrix at index
        if (!indexToConnectedHashSetMap.containsKey(index) || matrix[index.getRow()][index.getCol()] == 0) {
            return;
        }

        // access to the index-to-connected_component mapping is synchronized using a write lock
        connectedHashSetMatrixLock.writeLock().lock();

        if (indexToConnectedHashSetMap.get(index) == null) { // index not yet visited
            HashSet<Index> connectedHashSet;
            if (prevIndex == null) { // this is the first index in the component
                // create a new HashSet for this index (start a new connected component)
                connectedHashSet = new HashSet<>();
                connectedHashSet.add(index);
            } else { // the component was already initialized
                // add current index to existing component
                connectedHashSet = indexToConnectedHashSetMap.get(prevIndex);
                connectedHashSet.add(index);
            }

            // map index to its connected component hashset
            indexToConnectedHashSetMap.put(index, connectedHashSet);

            connectedHashSetMatrixLock.writeLock().unlock();

            // try crawling to all 8 directions
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue; // same as current index
                    Index nextIndex = new Index(index.getRow() + i, index.getCol() + j);
                    crawlAtLocation(nextIndex, index, indexToConnectedHashSetMap, matrix, connectedHashSetMatrixLock);
                }
            }
        } else { // index was already visited
            // if crawl has just started (prevIndex == null) do nothing
            if (prevIndex != null) {
                // check if index was visited from current component, or from a different component
                // if visited from current component, do nothing
                HashSet<Index> thisComponentHashSet = indexToConnectedHashSetMap.get(prevIndex);
                HashSet<Index> otherComponentHashSet = indexToConnectedHashSetMap.get(index);

                if (thisComponentHashSet != otherComponentHashSet) {
                    // index was visited from a different component
                    // this means this component and other component are connected
                    // merge this and other component and set all indices in this component
                    // to point to other component
                    otherComponentHashSet.addAll(thisComponentHashSet);

                    for (Index ind : thisComponentHashSet) {
                        indexToConnectedHashSetMap.put(ind, otherComponentHashSet);
                    }
                }
            }

            connectedHashSetMatrixLock.writeLock().unlock();
        }
    }

    // shuts down the executor service, and waits (blocks the current thread) for the already submitted tasks to finish
    // throws AlgorithmThreadingException if waiting for tasks to finish caused an exception
    private static void shutdownAndWaitForTasksToFinish(ExecutorService executorService) throws AlgorithmThreadingException {
        executorService.shutdown(); // do not accept any new tasks (but wait until all existing tasks are finished)

        try {
            // wait 1000 seconds for executor to finish the work
            // if 1000 seconds passed and executor is still not finished, wait 1000 seconds again
            while (!executorService.awaitTermination(1000L, TimeUnit.SECONDS)) {

            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new AlgorithmThreadingException(e.getMessage());
        }
    }

    //matrixGenerator receives integer num
    //return matrix n x n
    public static int[][] matrixGenerator(int n) {
        Random r = new Random();
        int[][] matrix = new int[n][n];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = r.nextInt(2);
            }
        }
        System.out.println("matrix "+n+" x "+n);
        for (int[] row : matrix) {
            String s = Arrays.toString(row);
            System.out.println(s);
            }
            System.out.println("\n");
        return  matrix;
        }



}