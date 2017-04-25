package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by wangxiandeng on 2016/11/25.
 * Changed by pingsh on 2017/4/25
 */

public class AccessibilityUtil {
    private static final String TAG = "应用";

    public static void doBack(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }


    public static void doPullDown(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
    }

    public static void doPullUp(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    public static void doRight(AccessibilityService service) {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (services != null) {
                return services.toLowerCase().contains(context.getPackageName().toLowerCase());
            }
        }

        return false;
    }


    public static void doLeft(AccessibilityService service) {
        String currentApp = "NULL";
        String secondApp = "NULL";
        String thirdApp = "NULL";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) service.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            for (int i = 0; i < appList.size(); i++) {
                Log.e(TAG, "all app: " + appList.get(i).getPackageName());
            }
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    List<Long> timeList = new ArrayList<>();
                    for (Map.Entry<Long, UsageStats> entry : mySortedMap.entrySet()) {
                        Log.e(TAG, "Key = " + entry.getKey() + ", Value = " + entry.getValue().getPackageName());
                        //如果最近的进程是桌面,则切换到桌面的前一个进程
                        timeList.add(entry.getKey());
                        if (timeList.size() == mySortedMap.size() - 2) {
                            thirdApp = entry.getValue().getPackageName();
                            Log.e(TAG, "thirdApp = " + thirdApp);
                        }

                        if (timeList.size() == mySortedMap.size() - 1) {
                            secondApp = entry.getValue().getPackageName();
                            Log.e(TAG, "secondAPP = " + entry.getValue().getPackageName());
                        }

                    }

                    if (currentApp.contains("launcher")) {
                        Toast.makeText(service, "当前已是桌面", Toast.LENGTH_SHORT).show();
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    } else if (secondApp.contains("launcher")) {
                        Intent intent_third = service.getPackageManager().getLaunchIntentForPackage(thirdApp);
                        service.startActivity(intent_third);
                    } else {
                        Intent intent_second = service.getPackageManager().getLaunchIntentForPackage(secondApp);
                        service.startActivity(intent_second);
                    }

                }
            }
        } else {
            ActivityManager am = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e(TAG, "Current App in foreground is: " + currentApp);

    }
}
