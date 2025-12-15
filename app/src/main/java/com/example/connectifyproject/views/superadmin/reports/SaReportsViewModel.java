package com.example.connectifyproject.views.superadmin.reports;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SaReportsViewModel extends ViewModel {

    private static final String TAG = "SaReportsViewModel";

    public final MutableLiveData<ReportsSummary> summary = new MutableLiveData<>();
    public final MutableLiveData<List<CompanyStat>> companies = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<List<CompanyStat>> top5 = new MutableLiveData<>(new ArrayList<>());
    
    // Estado de carga: true cuando está cargando, false cuando terminó
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // Indica si los datos ya fueron cargados al menos una vez
    public final MutableLiveData<Boolean> dataReady = new MutableLiveData<>(false);

    // filtros
    public int selectedYear;
    public MonthFilter selectedMonth;
    public String selectedCompanyIdOrAll = "ALL";

    private final SaReportsRepository repo = new SaReportsRepository();

    public void load(int year, MonthFilter month){
        selectedYear = year;
        selectedMonth = month;
        
        // Indicar que estamos cargando
        isLoading.setValue(true);
        dataReady.setValue(false);
        
        Log.d(TAG, "Iniciando carga de datos para " + month + "/" + year);

        repo.loadMonthData(year, month, new SaReportsRepository.LoadCallback() {
            @Override
            public void onLoaded(ReportsSummary sum, List<CompanyStat> all) {
                Log.d(TAG, "Datos recibidos - Summary: " + (sum != null) + ", Companies: " + (all != null ? all.size() : 0));
                
                // Actualizar todos los LiveData
                companies.postValue(all);
                computeTop5(all);
                summary.postValue(sum);
                
                // Indicar que terminamos de cargar
                isLoading.postValue(false);
                dataReady.postValue(true);
                
                Log.d(TAG, "Carga completada, dataReady=true");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error cargando datos", e);
                isLoading.postValue(false);
                dataReady.postValue(false);
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
