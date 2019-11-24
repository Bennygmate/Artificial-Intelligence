#!/bin/sh
echo -n `./ass1q3.py '[] seq [(neg p) or p]'`
echo -n `./ass1q3.py '[neg(p or q)] seq [(neg p)]' >> tmp.txt`
echo -n `./ass1q3.py '[p] seq [q imp p]' >> tmp.txt`
echo -n `./ass1q3.py '[p] seq [p or q]' >> tmp.txt`
echo -n `./ass1q3.py '[(p and q) and r] seq [p and (q and r)]' >> tmp.txt`
echo -n `./ass1q3.py '[p iff q] seq [neg(p iff (neg q))]' >> tmp.txt`
echo -n `./ass1q3.py '[p iff q] seq [(q iff r) imp (p iff r)]' >> tmp.txt`
echo -n `./ass1q3.py '[] seq [((neg p) and (neg q)) imp (p iff q)]' >> tmp.txt`
echo -n `./ass1q3.py '[p iff q] seq [(p and q) or ((neg p) and (neg q))]' >> tmp.txt`
echo -n `./ass1q3.py '[p imp q, (neg r) imp (neg q)] seq [p imp r]' >> tmp.txt`