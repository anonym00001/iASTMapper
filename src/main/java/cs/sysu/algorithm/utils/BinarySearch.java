package cs.sysu.algorithm.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Binary Search Implementation
 *
 * Given a list of numbers, perform binary search to find an index that satisfy a condition.
 */
public class BinarySearch {

    /**
     * Find index of the largest number that is smaller than or equal to the given number
     * @param numbers a list of integers in ascending order.
     * @param number the number.
     * @return the index of the number satisfying the condition.
     */
    public static int indexOfNumberSmallerOrEqual(List<Integer> numbers, int number){
        List<Integer> tmpNumbers = new ArrayList<>(numbers);
        if (tmpNumbers.get(0) > number)
            return -1;
        if (tmpNumbers.size() == 1)
            return 0;
        if (tmpNumbers.size() == 2){
            int right = tmpNumbers.get(1);
            if (right > number)
                return 0;
            else
                return 1;
        }
        int medIdx = tmpNumbers.size() / 2;
        List<Integer> leftIndexes = tmpNumbers.subList(0, medIdx);
        List<Integer> rightIndexes = tmpNumbers.subList(medIdx, numbers.size());
        int medNumber = tmpNumbers.get(medIdx);
        int medRightNumber = tmpNumbers.get(medIdx + 1);

        if (medNumber <= number && medRightNumber > number)
            return medIdx;

        if (medNumber > number)
            return indexOfNumberSmallerOrEqual(leftIndexes, number);
        else
            return medIdx + indexOfNumberSmallerOrEqual(rightIndexes, number);
    }

    /**
     * Find the index of the smallest number that is larger than or equal to the
     * @param numbers a list of integers in ascending order
     * @param number the number
     * @return the index of the number satisfying the condition.
     */
    public static int indexOfNumberLargerOrEqual(List<Integer> numbers, int number){
        List<Integer> tmpNumbers = new ArrayList<>(numbers);
        if (tmpNumbers.get(tmpNumbers.size()-1) < number)
            return -1;
        if (tmpNumbers.size() == 1)
            return 0;
        if (tmpNumbers.size() == 2){
            int left = tmpNumbers.get(0);
            if (left >= number)
                return 0;
            else
                return 1;
        }
        int medIdx = tmpNumbers.size() / 2;
        List<Integer> leftIndexes = tmpNumbers.subList(0, medIdx + 1);
        List<Integer> rightIndexes = tmpNumbers.subList(medIdx, numbers.size());
        int medNumber = tmpNumbers.get(medIdx);
        int medRightNumber = tmpNumbers.get(medIdx + 1);

        if (medNumber < number && medRightNumber >= number)
            return medIdx + 1;

        if (medNumber >= number)
            return indexOfNumberLargerOrEqual(leftIndexes, number);
        else
            return medIdx + indexOfNumberLargerOrEqual(rightIndexes, number);
    }
}
