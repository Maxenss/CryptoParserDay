package com.easylabs.cryptoparserday;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // Раз в 1 мин. получать значения цены криптовалют:
    // BTC, XPR, ETH
    // Данные будем брать с API. Данные получаем в json, и парсим.

    // Контейнер со всеми LinearLayout монет
    LinearLayout LLCoins;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LLCoins = (LinearLayout) findViewById(R.id.llCoins);

        btAddCoin = (Button) findViewById(R.id.btAddCoin);
        btAddCoin.setOnClickListener(this);
        etNewCoin = (EditText) findViewById(R.id.etNewCoin);

        //  // Создаём асинхронный поток
        //  RequestGetCoinsCost requestGetCoinsCost =
        //          new RequestGetCoinsCost();
        //  // Запускаем его
        //  requestGetCoinsCost.execute();

        Data.setActivity(this);
        if (isFirstLaunch) {
            Data.addCoin(new Coin("BTC"));
            Data.addCoin(new Coin("ETH"));
            Data.addCoin(new Coin("XRP"));
        } else {
            Data.readData();
        }

        createCoinsLL();
    }

    // Из макета создаём контейнер с инфой о монете
    private void createCoinLL(Coin coin) {
        LinearLayout linearLayout =
                (LinearLayout) LayoutInflater.from(this).
                        inflate(R.layout.coinll, null);

        // Отображаем названи
        ((TextView) (linearLayout.getChildAt(0))).setText(coin.getName());
        ((TextView) (linearLayout.getChildAt(1))).
                setText(String.valueOf(coin.getValue()));
        ((TextView) (linearLayout.getChildAt(2))).
                setText(String.valueOf(coin.getDiv()));

        // Объект с инф. о параметрах вставки компонента
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(10, 10, 10, 10);

        LLCoins.addView(linearLayout, layoutParams);
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

    public void btAddCoinClick() {
        if (etNewCoin.getVisibility() == View.GONE) {
            etNewCoin.setVisibility(View.VISIBLE);
        } else {
            String etNewCoinText = etNewCoin.getText().toString();

            if (etNewCoinText.isEmpty()) {
                Toast.makeText(MainActivity.this,
                        "Enter some coin name",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Coin coin = new Coin(etNewCoinText);
            if (Data.addCoin(coin)) {
                createCoinLL(coin);
            } else {
                Toast.makeText(this, "Эта монета уже есть :)", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*
    // Отображаем инф. о монетах на экран
    private void showCoins() {
        for (int i = 0; i < coins.length; i++) {
            tvNames[i].setText(coins[i].getName());
            tvValues[i].setText(coins[i].getValue() + "$");
            double div = coins[i].getDiv();
            // Отображать только два знака после запятой
            String divString = String.format("%.2f", div);
            // 0.0224215363624371235161
            // 0.02
            if (div < 0) {
                // Изменяем цвет текса текстового поля на красный
                tvDivs[i].setTextColor(Color.parseColor("#ff0000"));
                tvDivs[i].setText(divString + "%");
            } else if (div == 0) {
                // Изменяем цвет текса текстового поля на красный
                tvDivs[i].setTextColor(Color.parseColor("#f1f1f1"));
                tvDivs[i].setText(divString + "%");
            } else {
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
            double cost = jsonObject.getDouble("USD");

            return cost;
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    // Асинхронный поток для получения информации о значениях криптовалют
    class RequestGetCoinsCost extends AsyncTask<Void, Void, Void> {
        // Выполняет работу в фоновом потоке
        // НЕ ИМЕЕТ ДОСТУПА К GUI
        @Override
        protected Void doInBackground(Void... voids) {
            boolean flag = true;

            // Получаем информацию о валютах
            while (flag) {
                for (int i = 0; i < coins.length; i++) {
                    double cost = coinmarketRequset(coins[i].getName());
                    coins[i].setValue(cost);
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

        // ИМЕЕТ ДОСТУП К ОСНОВНОМУ ПОТОКУ, ВЫЗЫВАЕТСЯ ИЗ doInBackground
        @Override
        protected void onProgressUpdate(Void... values) {
            // Выводим информацию о криптовалютах на экран
            showCoins();
            super.onProgressUpdate(values);
        }
    }

    // Асинхронный поток для  получения информации об одной криптовалюте
    class RequestOneCoinPrice extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Получаем курс одном монеты
            // Получаем по индексу indexOneCoin монету, получаем её имя
            // и запрашиваем её курс
            double cost = coinmarketRequset(coins[indexOneCoin].getName());
            // Изменяем значения текущего курса для монеты
            // под индексом indexOneCoin
            coins[indexOneCoin].setValue(cost);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Отображаем информацию о всех монетах
            showCoins();
            String coinName = coins[indexOneCoin].getName();
            Toast.makeText(MainActivity.this,
                    "Получили курс - " + coinName,
                    Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }

    // Асинхронный поток для  получения информации об одной криптовалюте
    class RequestOneCoinPriceName extends AsyncTask<Void, Void, Void> {
        double cost = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            // Получаем курс одном монеты
            // Если монета с именем coinName существует, то метод вернет её значение
            // если монеты с таким именем нет, метод вернет -1
            cost = coinmarketRequset(coinName);

            return null;
        }

        // Выполняется после doInBackground
        @Override
        protected void onPostExecute(Void aVoid) {
            // Если cost < 0, то валюты с именем coinName нет
            if (cost < 0) {
                Toast.makeText(MainActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                // Иначе, если cost >=0 то валюта с таким именем есть
            } else {
                Toast.makeText(MainActivity.this, "Ошибки не было", Toast.LENGTH_SHORT).show();
                // Добавить логику добавления монеты в список
            }

            // После того, как мы получили ответ по запрошенному имени монеты
            // скрыть поле для ввода имени новой монеты
            etNewCoin.setVisibility(View.GONE);

            super.onPostExecute(aVoid);
        }
    }

    int indexOneCoin = 0;
    String coinName;

    View.OnClickListener onCoinClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // view - объект на котором было совершенн нажатие
            int index = -1;

            // llCoins в котором содержится информация о всех контейнерах
            // LinearLayout с монетами
            for (int i = 0; i < llCoins.length; i++) {
                // Если текущий контейнер LinearLayout по индексу i,
                // соотв. объекту на котором было соверщенно нажатие
                // то присвоить его индекс переменной index
                // и  завершить выполнение цикла
                if (llCoins[i].equals(view)) {
                    index = i;
                    break;
                }
            }

            // Если не нашли вхождение объекта в массив
            // Не нашли контейнера на который нажимали в массиве
            // Делаем завершение выполнение метода
            if (index == -1) return;

            // Переменной класса indexOneCoin присваиваем значение переменной index
            indexOneCoin = index;

            // Ассинхронный поток, для получения информации о одной монете
            RequestOneCoinPrice requestOneCoinPrice =
                    new RequestOneCoinPrice();
            // Помещается в очередь на выполнение
            // По-умолчанию, все асинхронные потоки,
            // выполняются по принципу
            // Создается поток 1, срок жизни которого равен 10 секунд, и запускается поток на выполение
            // Через 3 сек. создаем асинхр. поток 2, и запускаем его.
            // Поток 2, не будет выполняться вовсе., если при его вызове использовался
            // след. синтаксис: asyncTaskName.execute();
            // Если Вам необходимо, чтобы задачи выполнялись "парралельно",
            // то при вызове потока на исполнения необходимо применять
            // синтаксис:
            // asyncTaskName.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            requestOneCoinPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            // 1 - изначально
            // x - потоков может быть
            // > x && RAM.ISFree() --> x*y
            // 127 потоков
        }
    };

    // Метод обработичка на нажатие на кнопку "добаваить монету"
    View.OnClickListener onButtonAddCoinClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // etNewCoin.getVisibility() - получаем статус видимости компонента
                    // View.GONE - компонент не виден, и не занимает место в контейнере
                    // View.VISIBLE - компонент виден, и занимает место в контейнере
                    if (etNewCoin.getVisibility() == View.GONE) {
                        etNewCoin.setVisibility(View.VISIBLE);
                    } else {
                        // Считывем текст с поля для ввода
                        String etNewCoinText = etNewCoin.getText().toString();

                        // Если строка etNewCoinText пуста, то выводим сообщение
                        // что строка пуста, и делаем возврат из метода
                        if (etNewCoinText.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    "Enter some coin name",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Переменной класса coinName присваиваем значения текста
                        // с текстового поля, возведенное в верхний регистр
                        // btc - error
                        // BTC - JSON
                        // BTC, btc, Btc
                        // toUpperCase() - метод для возведения строки к верхнему регистру
                        coinName = etNewCoinText.toUpperCase();

                        // Запрашиваем курс одной монеты, по её имени
                        // если мы курс получаем, то добавляем монету
                        // иначе, если мы получаем ощибку то выводим на экран,что всё очень плохо
                        RequestOneCoinPriceName requestOneCoinPriceName =
                                new RequestOneCoinPriceName();
                        requestOneCoinPriceName.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        // etNewCoin.setVisibility(View.GONE);
                    }
                }
            };
            */

    private void testMethod() {
        Customer customer = new Customer();
        customer.setName("Alex").
                setLastName("Black").
                setEmail("customer@gmail.com");
    }
}