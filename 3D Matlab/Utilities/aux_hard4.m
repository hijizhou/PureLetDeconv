function [theta, theta1, theta2] = aux_hard4(factor, y, T, Tp)
%AUX_HARD4: Computes some partial derivatives of the
%   thresholding function.
%
% USAGE: [theta, theta1, theta2] = aux_hard4(factor, y, T, Tp)
%
% INPUT:  
%       factor, T, Tp -- the thresholding function parameter
%       y      -- the noisy image
%
% OUTPUT: theta, theta1, theta2 -- the thresholded component and partial
%       derivatives.
%
% See also aux_thresh
%
% Author: Jizhou Li, Florian Luisier and Thierry Blu
%
% References:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% Contact:
%   Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

T  = factor*T;
T2 = T.*T;
T4 = T2.*T2;
T5 = T.*T4;
y2 = y.*y;
y3 = y.*y2;
y4 = y2.*y2;
g  = exp(-y4./T4);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Tp  = factor*Tp;
g1  = -4./T4.*y3.*g;
g2  = 4./T5.*Tp.*y4.*g;
% g12 = 4./T5.*Tp.*y3.*(4*g+y.*g1);
% Tpp = Thresh.factor*Thresh.Tpp;
% g11 = -4./T4.*y2.*(3*g+y.*g1);
% g22 = 4./T5.*y4.*(-5*Tp.*Tp.*g./T+Tpp.*g+Tp.*g2);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
theta   = y.*(1-g);
theta1  = 1-g-y.*g1;
theta2  = -y.*g2;
% Theta.theta12 = -(g2+y.*g12);
% Theta.theta11 = -(2*g1+y.*g11);
% Theta.theta22 = -y.*g22;