///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github.lalyos:jfiglet:0.0.9

import com.github.lalyos.jfiglet.FigletFont;

public class hello {
    public static void main(String[] args) throws Exception {
        String asciiArt = FigletFont.convertOneLine("Hello " + args[0]);
        System.out.println(asciiArt);
    }
}
