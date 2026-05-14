package com.bankqueue.model;

public class Customer {

    private static int idCounter = 1;

    public enum Type { NORMAL, VIP }

    private final int    id;
    private final String name;
    private final int    arrivalSecond;
    private final Type   type;
    private       int    servedSecond = -1;
    private       int    cashierNo    = -1;
    private       boolean served      = false;

    public Customer(String name, int arrivalSecond, Type type) {
        this.id            = idCounter++;
        this.name          = name;
        this.arrivalSecond = arrivalSecond;
        this.type          = type;
    }

    public void    serve(int sec, int cashier) { served = true; servedSecond = sec; cashierNo = cashier; }
    public int     getWaitTime()      { return served ? (servedSecond - arrivalSecond) : -1; }
    public int     getId()            { return id;            }
    public String  getName()          { return name;          }
    public int     getArrivalSecond() { return arrivalSecond; }
    public Type    getType()          { return type;          }
    public int     getServedSecond()  { return servedSecond;  }
    public int     getCashierNo()     { return cashierNo;     }
    public boolean isServed()         { return served;        }

    public static void resetCounter()     { idCounter = 1; }
    public static int  getCounter()       { return idCounter; }
    public static void setCounter(int v)  { idCounter = v; }

    public String toCsv() {
        return id + "," + name + "," + arrivalSecond + "," + servedSecond
            + "," + cashierNo + "," + type.name() + "," + getWaitTime();
    }

    @Override public String toString() {
        return String.format("[%s] %s (#%03d)", type == Type.VIP ? "VIP" : "NRM", name, id);
    }
}

