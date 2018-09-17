function [ff1, ff2, div1, div2] = eleFun(Hbit, Y, Hli, Yi, sigma2, Dx, Dy, Dz, alpha,j)
%%ELEFUN: Processing function for each subband
% (more friendly for parallel processing)
%
% USAGE: [ff1, ff2, div1, div2] = eleFun(Hbit, Y, Hli, Yi, sigma2, Dx, Dy, Dz, alpha,j)
%
% AUTHORS: Jizhou Li, Florian Luisier and Thierry Blu
%
% REFERENCES:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution,
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach,
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images,
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% CONTACT: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

DD = reshape(Dx,size(Y,1),1,[])*reshape(Dy,1,size(Y,2),[]);
D = bsxfun(@times, DD, reshape(Dz,1,1,[]));
R = conj(D)/8^j;

Dx=[]; Dy=[]; Dz=[]; DD=[];
Di = D.*Hli;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
w      = real(ifftn(D.*Yi));
D = [];

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
di   = real(ifftn(Di));
sig2   = sigma2*mean2(abs(Di).^2);

di2  = di.*di;
Di2  = fftn(di2);
v      = alpha*real(ifftn(Di2.*Y));
Di =[];
Di2 = [];

[T Tp] = aux_thresh(v,sig2,1);
OK = mean2(w.*w)>2*mean2(T.^2);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clear v;

if(OK)
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    rt    = real(ifftn(conj(Hbit.*R)));
    
    DiRt  = fftn(di.*rt);
    Di2Rt = fftn(di2.*rt);
    clear rt di di2 Hbit Hli;
    
    w1    = real(ifftn(DiRt.*Y));
    w2    = real(ifftn(Di2Rt.*Y));
    
    clear Y DiRt Di2Rt;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %                 diagDR  = real(mean2(Di.*Hbit.*R));
    %                 Di3     = fftn(di.*di2);
    %                 diagD3R = real(mean2(Di3.*Hbit.*R));
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    factor = 4;
    [theta, theta1, theta2]  = aux_hard4(factor, w,T, Tp);
    
    Fk     = R.*fftn(theta);
    ff1 = Fk(:);
    
    eleDiv = alpha*theta1(:)'*w1(:);
    div1 = eleDiv+alpha*theta2(:)'*w2(:);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    factor = 9;
    [theta, theta1, theta2]  = aux_hard4(factor, w,T, Tp);
    
    Fk     = R.*fftn(theta);
    ff2 = Fk(:);
    
    eleDiv = alpha*theta1(:)'*w1(:);
    div2 = eleDiv+alpha*theta2(:)'*w2(:);
    
else
    ff1 = []; ff2 = []; div1=[]; div2=[];
end

end