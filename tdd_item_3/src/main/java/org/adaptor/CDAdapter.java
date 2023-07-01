package org.adaptor;

// 注意：适配器类的m命名不一定非得末尾带Adapter
public class CDAdapter extends CD implements ITarget{
    @Override
    public void function1() {
        super.lowPerformanceFunction4();
    }

    @Override
    public void function2() {
        staticFunction1();
    }

    @Override
    public void function4() {
        super.tooManyParamsFunction(1, 2);
    }
}
