package ind.lcw.campus_seckill.access;


import ind.lcw.campus_seckill.entity.SeckillUser;

public class UserContext {
	private static ThreadLocal<SeckillUser> userHolder=new ThreadLocal<SeckillUser>();

	public static void setUser(SeckillUser user) {
		userHolder.set(user);
	}

	public static SeckillUser getUser() {
		return userHolder.get();
	}
}
