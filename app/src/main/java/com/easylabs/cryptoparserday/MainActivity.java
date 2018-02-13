package com.easylabs.cryptoparserday;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener {
    // TODO: проверку, перед добавлением монеты, есть ли она в реальности
    // TODO: сделать code review
    // TODO: интегрировать рекламу

    // Контейнер со всеми LinearLayout монет
    LinearLayout LLCoins;

    private static final String TAG = "MainActivity";

    private AdView mAdView;

    // Способ хранения монет:
    // 1. БД.
    // 2. SharedPref и JSON.
    // BTC, BCC
    // {coins:[{name:"BTC"}, {name: "BCC"}]}

    // Кнопка, отвечающая за добавления новой монеты
    Button btAddCoin;
    // Поле для ввода названия монеты для добавления
    EditText etNewCoin;

    boolean isFirstLaunch = true;

    ArrayList<LinearLayout>llCoinsList = new ArrayList<>();

    // Индекс монеты на которую мы нажали
    int coinIndex;

    private  SharedPreferences sPref;

    private String FIRST_LAUNCH = "FIRST_LAUNCH";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        LLCoins = (LinearLayout) findViewById(R.id.llCoins);

        btAddCoin = (Button) findViewById(R.id.btAddCoin);
        btAddCoin.setOnClickListener(this);
        etNewCoin = (EditText) findViewById(R.id.etNewCoin);

        // Проверяем первый запуск, или не первый
        // Иниц. объект для работы с SharedPref
        sPref = getPreferences(MODE_PRIVATE);

        isFirstLaunch = sPref.getBoolean(FIRST_LAUNCH, true);

        //  // Создаём асинхронный поток
        //  RequestGetCoinsCost requestGetCoinsCost =
        //          new RequestGetCoinsCost();
        //  // Запускаем его
        //  requestGetCoinsCost.execute();

        Data.setActivity(this);
        if (isFirstLaunch) {
            System.out.println("Первый запуск");
            Data.addCoin(new Coin("BTC"));
            Data.addCoin(new Coin("ETH"));
            Data.addCoin(new Coin("XRP"));

            sPref = getPreferences(MODE_PRIVATE);
            // Подгот. изменения
            SharedPreferences.Editor ed = sPref.edit();
            // Помещаем данные в файл
            ed.putBoolean(FIRST_LAUNCH, false);

            ed.commit();
        } else {
            System.out.println("Не первый запуск");
            Data.readData();
        }

        createCoinsLL();

        RequestCoinsPrice requestCoinsPrice =
                new RequestCoinsPrice();
        requestCoinsPrice.execute();
    }

    // Метод вызываемый при долгом нажатии на View компонент,
    // который был зарег. для контекстного меню
    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        llCoin = (LinearLayout) v;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    LinearLayout llCoin;
    // Метод вызываемый при нажатии на один из пунктов меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Получаем индекс монеты
        int indexLLCoin = llCoinsList.indexOf(llCoin);

        switch (item.getItemId()) {
            case R.id.delete:
                Data.removeCoin(indexLLCoin);
                llCoin.setVisibility(View.GONE);
                LLCoins.removeView(llCoin);
                llCoinsList.remove(llCoin);

                break;
        }

        return super.onContextItemSelected(item);
    }

    // Из макета создаём контейнер с инфой о монете
    private void createCoinLL(Coin coin) {
        LinearLayout llCoin =
                (LinearLayout) LayoutInflater.from(this).
                        inflate(R.layout.coinll, null);

        // Отображаем названи
        ((TextView) (llCoin.getChildAt(0))).setText(coin.getName());
        ((TextView) (llCoin.getChildAt(1))).
                setText(String.valueOf(coin.getValue()));
        ((TextView) (llCoin.getChildAt(2))).
                setText(String.valueOf(coin.getDiv()));

        // Объект с инф. о параметрах вставки компонента
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(10, 10, 10, 10);

        // Привяжем обработчик на нажатие на контейнер с монетой
        llCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout llCoin = (LinearLayout)view;

                coinIndex = llCoinsList.indexOf(llCoin);

                // Скрываем видимость поля для ввода, при нажатии на любую из монет
                etNewCoin.setVisibility(View.GONE);

                RequestOneCoinPrice requestOneCoinPrice =
                        new RequestOneCoinPrice(coinIndex);
                requestOneCoinPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        // Регистрируем контейнер с монетой, для контекстного меню
        registerForContextMenu(llCoin);

        llCoinsList.add(llCoin);
        LLCoins.addView(llCoin, layoutParams);
    }

    // Создаём несколько контейнеров
    private void createCoinsLL() {
        for (Coin coin :
                Data.getCoinsArrayList()) {
            createCoinLL(coin);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btAddCoin:
                btAddCoinClick();
                break;
        }
    }

    // Метод, вызываемый при добавлении монеты в коллекцию
    public void btAddCoinClick() {
        if (etNewCoin.getVisibility() == View.GONE) {
            etNewCoin.setVisibility(View.VISIBLE);
        } else {
            // Считываем название монеты с поля для ввода
            String etNewCoinText = etNewCoin.getText().toString().toUpperCase(); // BTC

            if (etNewCoinText.isEmpty()) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.newCoinHint),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            etNewCoin.setText("");

            RequestIsCorectNameCoin requestIsCorectNameCoin =
                    new RequestIsCorectNameCoin(etNewCoinText);
            requestIsCorectNameCoin.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void llCoinClick(View view){
        LinearLayout llCoin = (LinearLayout)view;

        int coinIndex = llCoinsList.indexOf(llCoin);

        // String coinName = Data.getCoinsArrayList().get(coinIndex).getName();
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
            double cost = jsonObject.getDouble("USD");

            return cost;
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    // Отображаем инф. о монетах на экран
    private void showCoins() {
        for (int i = 0; i < Data.getCoinsArrayList().size(); i++) {
            Coin coin = Data.getCoinsArrayList().get(i);
            LinearLayout llCoin = llCoinsList.get(i);
            double div = coin.getDiv();
            String divString = String.format("%.5f", div);

            ((TextView) (llCoin.getChildAt(0))).setText(coin.getName());
            ((TextView) (llCoin.getChildAt(1))).
                    setText(String.valueOf(coin.getValue()));

            TextView tvDiv = ((TextView) (llCoin.getChildAt(2)));

            // Если курс вырос
            if (div > 0) {
                tvDiv.setText("+" + divString + "%");
                // Цвет зеленый
                tvDiv.setTextColor(Color.parseColor("#32CD32"));
            }
            else if (div == 0) {
                tvDiv.setText(0 + "%");
                // Цвет черный
                tvDiv.setTextColor(Color.parseColor("#000000"));
            }
            else{
                tvDiv.setText(divString + "%");
                // Цвет красный
                tvDiv.setTextColor(Color.parseColor("#ff0000"));
            }
            }

        }

    // Асинхронный поток для  получения информации об одной криптовалюте
    class RequestOneCoinPrice extends AsyncTask<Void, Void, Void> {
        int index;

        public RequestOneCoinPrice(int coinIndex) {
            this.index = coinIndex;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            double cost =
                    coinmarketRequset(
                            Data.getCoinsArrayList().
                                    get(index).
                                    getName());
            // Изменяем значения текущего курса для монеты
            // под индексом indexOneCoin
            Data.getCoinsArrayList().get(index).setValue(cost);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Отображаем информацию о всех монетах
            showCoins();
            String coinName =  Data.getCoinsArrayList().
                    get(index).
                    getName();
            super.onPostExecute(aVoid);
        }
    }

    // Асинхронный поток для получения информации о множестве монет
    class RequestCoinsPrice extends AsyncTask<Void, Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            boolean flag = true;

            // Получаем информацию о валютах
            while (flag) {
                for (int i = 0; i < Data.getCoinsArrayList().size(); i++) {
                    Coin coin = Data.getCoinsArrayList().get(i);
                    double cost = coinmarketRequset(coin.getName());
                    coin.setValue(cost);
                }

                // Это вызов метода onProgressUpdate
                publishProgress();

                // Делаем задержку, между запросами
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // Выводим информацию о криптовалютах на экран
            showCoins();
        }

    }

    class RequestIsCorectNameCoin extends  AsyncTask<Void, Void, Void>{
        public RequestIsCorectNameCoin(String coinName) {
            this.coinName = coinName;
        }

        String coinName;
        double coinPrice;

        // Метод выполняющийся асинхроно, и никак не влияющий на основной поток приложения
        @Override
        protected Void doInBackground(Void... voids) {
            // Если монета существует, мы получим вещественное положительное число
            // Если же монета не сущесвтует, мы получим -1
            coinPrice =  coinmarketRequset(coinName);

            return null;
        }

        // Метод вызываемый после выполнения doInBackground
        @Override
        protected void onPostExecute(Void aVoid) {
            if (coinPrice != -1){
                Coin coin = new Coin(coinName);
                if (Data.addCoin(coin)) {
                    createCoinLL(coin);
                    // Скрываем поле для ввода новой монеты
                    etNewCoin.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.coinExist),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this,
                        getString(R.string.c404),
                        Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(aVoid);
        }
    }
}