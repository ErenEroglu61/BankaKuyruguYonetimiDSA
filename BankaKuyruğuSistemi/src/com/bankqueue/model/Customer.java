package com.bankqueue.model;

class Customer {

    private static int idCounter = 1;

    enum Type { NORMAL, VIP }

    private final int    id;
    private final String name;
    private final int    arrivalSecond;
    private final Type   type;
    private       int    servedSecond = -1;
    private       int    cashierNo    = -1;
    private       boolean served      = false;

    Customer(String name, int arrivalSecond, Type type) {
        this.id            = idCounter++;
        this.name          = name;
        this.arrivalSecond = arrivalSecond;
        this.type          = type;
    }

    void    serve(int sec, int cashier) { served = true; servedSecond = sec; cashierNo = cashier; }
    int     getWaitTime()      { return served ? (servedSecond - arrivalSecond) : -1; }
    int     getId()            { return id;            }
    String  getName()          { return name;          }
    int     getArrivalSecond() { return arrivalSecond; }
    Type    getType()          { return type;          }
    int     getServedSecond()  { return servedSecond;  }
    int     getCashierNo()     { return cashierNo;     }
    boolean isServed()         { return served;        }

    static void resetCounter()     { idCounter = 1; }
    static int  getCounter()       { return idCounter; }
    static void setCounter(int v)  { idCounter = v; }

    String toCsv() {
        return id + "," + name + "," + arrivalSecond + "," + servedSecond
            + "," + cashierNo + "," + type.name() + "," + getWaitTime();
    }

    @Override public String toString() {
        return String.format("[%s] %s (#%03d)", type == Type.VIP ? "VIP" : "NRM", name, id);
    }
}

