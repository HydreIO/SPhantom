package sceat.domain.forkupdate;

public enum ForkUpdateType {
	MIN_64(3840000L),
	MIN_32(1920000L),
	MIN_16(960000L),
	MIN_08(480000L),
	MIN_05(300000L),
	MIN_02(120000L),
	MIN_01(60000L),
	SEC_30(30000L),
	SEC_15(15000L),
	SEC_10(10000L),
	SEC_05(5000L),
	SEC_04(4000L),
	SEC_03(3000L),
	SEC_02(2000L),
	SEC_01(1000L),
	DEMI_SEC(500L),
	QUART_SEC(250L),
	FASTEST(125L),
	TICK(49L);

	private long _time;
	private long _last;
	private long _timeSpent;
	private long _timeCount;

	private ForkUpdateType(long time) {
		this._time = time;
		this._last = System.currentTimeMillis();
	}

	public boolean Elapsed() {
		if (System.currentTimeMillis() - this._last > this._time) {
			this._last = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	public void StartTime() {
		this._timeCount = System.currentTimeMillis();
	}

	public void StopTime() {
		this._timeSpent += System.currentTimeMillis() - this._timeCount;
	}

	public void PrintAndResetTime() {
		System.out.println(name() + " in a second: " + this._timeSpent);
		this._timeSpent = 0L;
	}
}
