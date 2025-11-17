package com.example.connectifyproject.views.superadmin.reports;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaReportsViewModel extends ViewModel {

    public final MutableLiveData<ReportsSummary> summary = new MutableLiveData<>();
    public final MutableLiveData<List<CompanyStat>> companies = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<CompanyStat>> top5 = new MutableLiveData<>(new ArrayList<>());

    // filtros
    public int selectedYear;
    public MonthFilter selectedMonth;
    public String selectedCompanyIdOrAll = "ALL";

    private final SaReportsRepository repo = new SaReportsRepository();

    public void load(int year, MonthFilter month){
        selectedYear = year;
        selectedMonth = month;

        repo.loadMonthData(year, month, new SaReportsRepository.LoadCallback() {
            @Override
            public void onLoaded(ReportsSummary sum, List<CompanyStat> all) {
                summary.postValue(sum);
                companies.postValue(all);
                computeTop5(all);
            }

            @Override
            public void onError(Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void applyCompanyFilter(String companyIdOrAll){
        this.selectedCompanyIdOrAll = (companyIdOrAll == null || companyIdOrAll.isEmpty())
                ? "ALL" : companyIdOrAll;
        List<CompanyStat> all = companies.getValue();
        if(all == null) return;
        computeTop5(all);
    }

    private void computeTop5(List<CompanyStat> all){
        // Si hay filtro por empresa → top5 vacío (desaparece el gráfico)
        if(!"ALL".equals(selectedCompanyIdOrAll)){
            top5.postValue(Collections.<CompanyStat>emptyList());
            return;
        }
        ArrayList<CompanyStat> cloned = new ArrayList<>(all);
        cloned.sort((a, b) -> Integer.compare(b.monthTotal, a.monthTotal));
        if(cloned.size() > 5) {
            cloned = new ArrayList<>(cloned.subList(0, 5));
        }
        top5.postValue(cloned);
    }
}
