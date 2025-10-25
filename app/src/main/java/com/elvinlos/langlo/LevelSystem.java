package com.elvinlos.langlo;

public class LevelSystem {
    private static final int BASE_POINTS = 50;  // Điểm base cho level 1
    private static final int POINTS_INCREMENT = 50;  // Mỗi level tăng thêm 50 điểm

    // Tính level từ totalScore
    public static int calculateLevel(int totalScore) {
        int level = 1;
        int pointsRequired = 0;

        while (pointsRequired <= totalScore) {
            pointsRequired += BASE_POINTS + (level - 1) * POINTS_INCREMENT;
            if (pointsRequired <= totalScore) {
                level++;
            }
        }

        return level;
    }

    // Tính tổng điểm cần để đạt một level cụ thể
    public static int getPointsForLevel(int level) {
        int totalPoints = 0;
        for (int i = 1; i < level; i++) {
            totalPoints += BASE_POINTS + (i - 1) * POINTS_INCREMENT;
        }
        return totalPoints;
    }

    // Tính điểm cần để lên level tiếp theo
    public static int getPointsToNextLevel(int totalScore) {
        int currentLevel = calculateLevel(totalScore);
        int pointsForCurrentLevel = getPointsForLevel(currentLevel);
        int pointsForNextLevel = getPointsForLevel(currentLevel + 1);
        return pointsForNextLevel - totalScore;
    }

    // Tính phần trăm hoàn thành level hiện tại
    public static int getCurrentLevelProgressPercent(int totalScore) {
        int currentLevel = calculateLevel(totalScore);
        int pointsForCurrentLevel = getPointsForLevel(currentLevel);
        int pointsForNextLevel = getPointsForLevel(currentLevel + 1);
        int pointsInCurrentLevel = pointsForNextLevel - pointsForCurrentLevel;
        int currentProgress = totalScore - pointsForCurrentLevel;

        return (currentProgress * 100) / pointsInCurrentLevel;
    }
}
