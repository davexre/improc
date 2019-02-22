package com.slavi.db.spy;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SpyXAResource<TT extends XAResource> extends Spy<TT> implements XAResource {

	public SpyXAResource(TT delegate) {
		super(delegate);
	}

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		t.commit(xid, onePhase);
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
		t.end(xid, flags);
	}

	@Override
	public void forget(Xid xid) throws XAException {
		t.forget(xid);
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return t.getTransactionTimeout();
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		return t.isSameRM(xares);
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		return t.prepare(xid);
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		return t.recover(flag);
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		t.rollback(xid);
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return t.setTransactionTimeout(seconds);
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {
		t.start(xid, flags);
	}
}
