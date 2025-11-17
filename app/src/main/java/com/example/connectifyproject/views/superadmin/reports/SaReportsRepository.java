package com.example.connectifyproject.views.superadmin.reports;

import static java.util.stream.Collectors.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SaReportsRepository {

    private static final String TAG = "sa-reports-repo";
    private static final String COL_COMPANIES = "companies";
    private static final String COL_RESERVAS  = "reservas";
    private static final String FIELD_ACTIVE  = "active";
    private static final String FIELD_NAME    = "name";
    private static final String FIELD_COMPANY = "companyId";
    private static final String FIELD_TIME    = "createdAtMillis"; // <— ajusta si tu campo difiere

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LoadCallback {
        void onLoaded(ReportsSummary summary, List<CompanyStat> allCompanies);
        void onError(Exception e);
    }

    public void loadMonthData(int year, MonthFilter month, @NonNull LoadCallback cb){
        // Rango de tiempo del mes
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month.number - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();

        c.add(Calendar.MONTH, 1);
        long endExclusive = c.getTimeInMillis();

        // 1) Traer empresas
        db.collection(COL_COMPANIES).get().addOnCompleteListener(companiesTask -> {
            if(!companiesTask.isSuccessful()){
                cb.onError(companiesTask.getException()); return;
            }
            List<DocumentSnapshot> docs = companiesTask.getResult().getDocuments();
            Map<String,CompanyStat> map = new LinkedHashMap<>();
            AtomicInteger activeCount = new AtomicInteger(0);

            for(DocumentSnapshot d: docs){
                String id = d.getId();
                String name = d.getString(FIELD_NAME);
                boolean active = Boolean.TRUE.equals(d.getBoolean(FIELD_ACTIVE));
                if(active) activeCount.incrementAndGet();
                map.put(id, new CompanyStat(id, name==null? id : name, active, 0));
            }

            // 2) Traer reservas del mes (todas) y contar por companyId
            db.collection(COL_RESERVAS)
                    .whereGreaterThanOrEqualTo(FIELD_TIME, start)
                    .whereLessThan(FIELD_TIME, endExclusive)
                    .get()
                    .addOnCompleteListener(resTask -> {
                        if(!resTask.isSuccessful()){
                            cb.onError(resTask.getException()); return;
                        }
                        int totalMes = 0;
                        for(DocumentSnapshot r: resTask.getResult()){
                            String cid = r.getString(FIELD_COMPANY);
                            if(cid==null) continue;
                            CompanyStat cs = map.get(cid);
                            if(cs!=null){ cs.monthTotal++; totalMes++; }
                        }

                        int daysOfMonth = daysInMonth(year, month.number);
                        double promedio = daysOfMonth==0? 0 : (totalMes * 1.0) / daysOfMonth;

                        ReportsSummary summary = new ReportsSummary(totalMes, activeCount.get(),
                                Math.round(promedio*100.0)/100.0);

                        cb.onLoaded(summary, new ArrayList<>(map.values()));
                    });
        });
    }

    private int daysInMonth(int year, int month1to12){
        Calendar cc = Calendar.getInstance();
        cc.set(Calendar.YEAR, year);
        cc.set(Calendar.MONTH, month1to12-1);
        return cc.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
