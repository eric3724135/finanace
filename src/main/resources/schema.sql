CREATE TABLE symbol (
    id varchar(20) NOT NULL PRIMARY KEY,
    name varchar(30) NOT NULL,
    type char(1)
);

CREATE TABLE quote (
 seq IDENTITY NOT NULL PRIMARY KEY,
 symbol varchar(20) NOT NULL,
 name varchar(30) NOT NULL,
 trade_date date NOT NULL,
 open double NOT NULL,
 high double NOT NULL,
 low double NOT NULL,
 close double NOT NULL,
 period varchar(10) NOT NULL,
 diff double NOT NULL,
 volume double ,
 avg_price double DEFAULT 0,
 ma5 double ,
 ma10 double ,
 ma20 double ,
 ma60 double ,
 ma120 double ,
 k9 double ,
 d9 double ,
 kd_diff double ,
 rsi5 double ,
 rsi10 double,
 UNIQUE (symbol,trade_date,period)
);

