class Main {
    public static void main(String[] a) { System.out.println(0); }
}

class Root {
    public int ping() { return 0; }
}

class Mid extends Root {
    public int pong() { return 1; }
}

class Leaf extends Mid {
    public int ping() { return 2; } // overrides Root.ping deep in the chain
    public int extra() { return 3; }
}
