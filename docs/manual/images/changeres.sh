# Change the res of all png images

# Pixels per meter for images
# pixperm=4724.5
pixperm=5905.51

all=`ls *.png`

for i in $all ; do
    pngtopnm $i > $i.pnm
done

rm $all

for i in $all ; do
    pnmtopng -interlace -phys $pixperm $pixperm 1 $i.pnm > $i
done

rm *.pnm

