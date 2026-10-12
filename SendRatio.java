public record SendRatio(int sends, int climbs){
    public double rate(){
        return (climbs == 0) ? 0.0 : (double) sends / climbs;
    }
}