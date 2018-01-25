package com.easylabs.cryptoparserday;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    // Раз в 1 мин. получать значения цены криптовалют:
    // BTC, XPR, ETH
    // Данные будем брать с API. Данные получаем в json, и парсим.

    // ДЗ: добавить ещё 7 криптовалют

    Coin[] coins = new Coin[3];
    TextView[] tvNames = new TextView[3];
    TextView[] tvValues = new TextView[3];
    TextView[] tvDivs = new TextView[3];

    TextView tvName1;
    TextView tvName2;
    TextView tvName3;

    TextView tvValue1;
    TextView tvValue2;
    TextView tvValue3;

    TextView tvDiv1;
    TextView tvDiv2;
    TextView tvDiv3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация View-компонентов
        tvName1 = (TextView)findViewById(R.id.tvName1);
        tvName2 = (TextView)findViewById(R.id.tvName2);
        tvName3 = (TextView)findViewById(R.id.tvName3);

        tvValue1 = (TextView)findViewById(R.id.tvValue1);
        tvValue2 = (TextView)findViewById(R.id.tvValue2);
        tvValue3 = (TextView)findViewById(R.id.tvValue3);

        tvDiv1 = (TextView)findViewById(R.id.tvDiv1);
        tvDiv2 = (TextView)findViewById(R.id.tvDiv2);
        tvDiv3 = (TextView)findViewById(R.id.tvDiv3);

        coins[0] = new Coin("BTC");
        coins[1] = new Coin("XRP");
        coins[2] = new Coin("ETH");

        tvNames[0] = tvName1;
        tvNames[1] = tvName2;
        tvNames[2] = tvName3;

        tvValues[0] = tvValue1;
        tvValues[1] = tvValue2;
        tvValues[2] = tvValue3;

        tvDivs[0] = tvDiv1;
        tvDivs[1] = tvDiv2;
        tvDivs[2] = tvDiv3;

        // Создаём асинхронный поток
        RequestGetCoinsCost requestGetCoinsCost =
                new RequestGetCoinsCost();
        // Запускаем его
        requestGetCoinsCost.execute();
    }

    // Отображаем инф. о монетах на экран
    private void showCoins(){
        for (int i = 0; i < coins.length; i++) {
            tvNames[i].setText(coins[i].getName());
            tvValues[i].setText(String.valueOf(coins[i].getValue()) + "$");
            double div = coins[i].getDiv();
            // Отображать только два знака после запятой
            String divString = String.format("%.2f", div);
            if (div < 0){
                // Изменяем цвет текса текстового поля на красный
                tvDivs[i].setTextColor(Color.parseColor("#ff0000"));
                tvDivs[i].setText(divString + "%");
            }
            else{
                // Изменяем цвет текса текстового поля на зеленый
                tvDivs[i].setTextColor(Color.parseColor("#00ff00"));
                tvDivs[i].setText("+" + divString + "%");
            }
        }
    }

    // Метод для получения информации о текущем курсе криптовалют на сервисе https://coinmarketcap.com/
    public double coinmarketRequset(String coinName) {
        String url = "https://min-api.cryptocompare.com/data/price?fsym=" + coinName + "&tsyms=USD";
        String responseData;

        try {
            // Указываем адрес для отправки запроса
            URL obj = new URL(url);
            // Открываем соединение
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // Строка, которая будет содержать ответ от сервера
            responseData = "";
            // Код ответа от сервера
            int responseCode;

            // Указываем тип запроса
            con.setRequestMethod("GET");

            responseCode = con.getResponseCode();
            System.out.println("nSending 'GET' request to URL : " + "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD");
            System.out.println("Response Code : " + responseCode);

            // Открываем поток для приема данных от сервера
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            // Считываем входящие данные
            while ((output = in.readLine()) != null) {
                response.append(output);
                System.out.println(output);
            }
            in.close();

            // Преобразовываем StringBuffer в String
            responseData = response.toString();
            // Создаём объект типа JSON
            JSONObject jsonObject = new JSONObject(responseData);
            double cost = Double.parseDouble(jsonObject.get("USD").toString());

            return cost;
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    // Асинхронный поток для получения информации о значениях криптовалют
    class RequestGetCoinsCost extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            boolean flag = true;

            while (flag){
                for (int i = 0; i < coins.length; i++) {
                    double cost = coinmarketRequset(coins[i].getName());
                    coins[i].setValue(cost);
                }
                // Это вызов метода onProgressUpdate
                publishProgress();

                // Делаем задержку, между запросами
                try {
                    Thread.sleep(10000);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return null;
        }

        // ИМЕЕТ ДОСТУП К ОСНОВНОМУ ПОТОКУ, ВЫЗЫВАЕТСЯ ИЗ doInBackground
        @Override
        protected void onProgressUpdate(Void... values) {
            // Выводим информацию о криптовалютах на экран
            showCoins();
            super.onProgressUpdate(values);
        }
    }
}