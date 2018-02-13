package com.easylabs.cryptoparserday;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Maxim on 30.01.2018.
 */

public class Data {
    // Для хранения монет
    private static ArrayList<Coin> coinsArrayList = new ArrayList<>();

    // Объект для работы с текстовым файлом
    private static SharedPreferences sPref;

    private static final String JSON_COINS = "JSON_COINS";

    private static Activity activity;

    // Читаем данные из SharedPref
    public static boolean readData() {
        try {
            // Иниц. объект для работы с SharedPref
            sPref = activity.getPreferences(MODE_PRIVATE);
            // Считываем json-строку
            String json = sPref.getString(JSON_COINS, "");

            coinsArrayList = new Gson().fromJson(json,
                    new TypeToken<ArrayList<Coin>>() {
                    }.getType());
            System.out.println(json);
            System.out.println(coinsArrayList.size());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Пишем данные в SharedPref
    public static boolean writeData() {
        try {
            // Создаём объект для работы с GSON
            Gson gson = new Gson();
            // Преобраз. коллекцию к массиву объектов JSON
            String json = gson.toJson(coinsArrayList);

            // Получаем ресурсы приложения
            // MODE_PRIVATE - означает, что только само приложение
            // может обращаться к этому файлу
            sPref = activity.getPreferences(MODE_PRIVATE);
            // Подгот. изменения
            SharedPreferences.Editor ed = sPref.edit();
            // Помещаем данные в файл
            ed.putString(JSON_COINS, json);
            // <string name="JSON_COINS">[{name="BTC"}, {name="ETH"}, {name="XRP"}]</>
            // Добавляем изменения в файл
            ed.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Метод для добавления монеты, вызывает writeData
    public static boolean addCoin(Coin coin) {
        for (Coin coinItem :
                coinsArrayList) {
            if (coinItem.getName().equals(coin.getName()))
                return false;
        }

        coinsArrayList.add(coin);
        writeData();

        return true;
    }

    // Метод для удаления монеты, вызывает writeData
    public static boolean removeCoin(Coin coin) {
        if(!coinsArrayList.contains(coin))
            return false;

        coinsArrayList.remove(coin);
        writeData();

        return true;
    }

    // Метод для удаления монеты, вызывает writeData
    public static boolean removeCoin(int index) {
        coinsArrayList.remove(index);
        writeData();

        return true;
    }

    // Метод для очистки данных
    public static boolean clearData() {
        coinsArrayList.clear();
        writeData();

        return true;
    }

    // Метод для получения коллекций с монетами
    public static ArrayList<Coin> getCoinsArrayList() {
        return coinsArrayList;
    }

    // Передаем контекст активити
    public static void setActivity(Activity _context) {
        activity = _context;
    }
}
