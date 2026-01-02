package org.example.springboot.DTO;

import lombok.Data;

import java.util.List;

/**
 * 统计数据传输对象
 */
@Data
public class StatisticsDTO {
    // 用户相关统计
    private Integer totalUserCount;
    private Integer maleUserCount;
    private Integer femaleUserCount;
    private Double userGrowthRate; // 较上月增长率
    
    // 宠物相关统计
    private Integer totalPetCount;
    private Integer adoptedPetCount;
    private Integer availablePetCount;
    private Double petGrowthRate; // 较上月增长率
    
    // 服务相关统计
    private Integer totalServiceAppointmentCount;
    private Integer completedServiceCount;
    private Integer pendingServiceCount;
    private Integer cancelledServiceCount;
    private Double serviceGrowthRate; // 较上月增长率
    
    // 训练相关统计
    private Integer totalTrainingCount;
    private Integer completedTrainingCount;
    private Integer averageRating;
    private Double trainingGrowthRate; // 较上月增长率
    
    // 寄养相关统计
    private Integer totalBoardingCount;
    private Integer activeBoardingCount;
    private Double boardingGrowthRate; // 较上月增长率
    
    // 各类数据的月度趋势数据
    private List<MonthlyStatDTO> userTrend;
    private List<MonthlyStatDTO> petTrend;
    private List<MonthlyStatDTO> serviceTrend;
    private List<MonthlyStatDTO> trainingTrend;
    private List<MonthlyStatDTO> boardingTrend;
} 