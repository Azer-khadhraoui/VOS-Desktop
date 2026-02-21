package org.example.services;

import org.example.entities.OffreEmploi;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for calculating statistics and analytics for job offers.
 * Provides methods for aggregating data by status, contract type, and time-based metrics.
 */
public class StatisticsService {

    private Connection connection;

    public StatisticsService() {
        this.connection = MyDataBase.getInstance().getConnection();
    }

    /**
     * Get total count of offers grouped by status.
     * 
     * @return Map with status as key and count as value
     */
    public Map<String, Integer> getOffresByStatus() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT statut_offre, COUNT(*) as count FROM offre_emploi GROUP BY statut_offre";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("statut_offre");
                int count = rs.getInt("count");
                stats.put(status, count);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching offers by status: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get total count of offers grouped by contract type.
     * 
     * @return Map with contract type as key and count as value
     */
    public Map<String, Integer> getOffresByContractType() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT type_contrat, COUNT(*) as count FROM offre_emploi GROUP BY type_contrat";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String contractType = rs.getString("type_contrat");
                int count = rs.getInt("count");
                stats.put(contractType, count);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching offers by contract type: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get total count of all offers in the database.
     * 
     * @return Total number of offers
     */
    public int getTotalOffers() {
        String sql = "SELECT COUNT(*) as total FROM offre_emploi";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching total offers: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get count of active offers.
     * 
     * @return Number of active offers
     */
    public int getActiveOffers() {
        String sql = "SELECT COUNT(*) as total FROM offre_emploi WHERE statut_offre = 'ACTIVE'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching active offers: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Calculate average number of days that offers stay active.
     * For active offers, calculates from publication date to current date.
     * For inactive/archived offers, would need an end date column (future enhancement).
     * 
     * @return Average days offers stay active
     */
    public double getAverageActiveTime() {
        String sql = """
            SELECT AVG(DATEDIFF(CURDATE(), date_publication)) as avg_days
            FROM offre_emploi
            WHERE statut_offre = 'ACTIVE' AND date_publication IS NOT NULL
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("avg_days");
            }

        } catch (SQLException e) {
            System.err.println("Error calculating average active time: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Get offers created in the last N days.
     * 
     * @param days Number of days to look back
     * @return Count of offers created in the period
     */
    public int getOffersInLastDays(int days) {
        String sql = "SELECT COUNT(*) as total FROM offre_emploi WHERE date_publication >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, days);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching offers in last " + days + " days: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get offers by work preference (On-site, Remote, Hybrid).
     * 
     * @return Map with work preference as key and count as value
     */
    public Map<String, Integer> getOffresByWorkPreference() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT work_preference, COUNT(*) as count FROM offre_emploi WHERE work_preference IS NOT NULL GROUP BY work_preference";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String workPref = rs.getString("work_preference");
                int count = rs.getInt("count");
                stats.put(workPref, count);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching offers by work preference: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get monthly trend data for offer creation.
     * Returns count of offers created per month for the last 12 months.
     * 
     * @return Map with month label as key and count as value
     */
    public Map<String, Integer> getMonthlyTrend() {
        Map<String, Integer> trend = new HashMap<>();
        String sql = """
            SELECT DATE_FORMAT(date_publication, '%Y-%m') as month, COUNT(*) as count
            FROM offre_emploi
            WHERE date_publication >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY month
            ORDER BY month
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                trend.put(month, count);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching monthly trend: " + e.getMessage());
            e.printStackTrace();
        }

        return trend;
    }

    /**
     * Get count of offers by location.
     * 
     * @return Map with location as key and count as value
     */
    public Map<String, Integer> getOffresByLocation() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT lieu, COUNT(*) as count FROM offre_emploi WHERE lieu IS NOT NULL AND lieu != '' GROUP BY lieu ORDER BY count DESC LIMIT 10";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String location = rs.getString("lieu");
                int count = rs.getInt("count");
                stats.put(location, count);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching offers by location: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Get comprehensive statistics summary.
     * 
     * @return Map containing various statistics
     */
    public Map<String, Object> getStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalOffers", getTotalOffers());
        summary.put("activeOffers", getActiveOffers());
        summary.put("averageActiveTime", getAverageActiveTime());
        summary.put("offersLast7Days", getOffersInLastDays(7));
        summary.put("offersLast30Days", getOffersInLastDays(30));
        summary.put("byStatus", getOffresByStatus());
        summary.put("byContractType", getOffresByContractType());
        summary.put("byWorkPreference", getOffresByWorkPreference());
        summary.put("byLocation", getOffresByLocation());
        
        return summary;
    }
}
