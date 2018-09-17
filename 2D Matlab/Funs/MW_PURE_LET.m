function output = MW_PURE_LET(params)
%% PURE-LET Image Deconvolution
% This set of codes is a Matlab implementation of PURE-LET deconvolution algorithms. 
% The mixed Poisson-Gaussian noise (including pure Poisson noise) is assumed.
%
% Model
% -----------  
%   Acquisition model: input = alpha*Poisson(H*original/alpha) + Gaussian(0, nsigma^2);
%   Point-spread function (PSF): H
%   Noise levels:
%           options.alpha : scaling factor of Poisson noise
%           options.nsigma: noise std of additive Gaussian noise
%
% Example (filtering):
% -------------------------------------------------------------------------
% % load the blurred noisy image
% >> input = double(imread('fluocells.tif')); 
% 
% % PSF, either acquired or calculated
% >> params.type = 'gaussian';
% >> params.size = size(input);
% >> params.input = input;
% >> params.var  = 0.01;
% >> params = aux_blur_operator(params);
% >> params = aux_reg_operator(params);
% 
% % noise settings: (provided or estimated)
% %   [alpha] scaling factor of Poisson noise; 
% %   [nsigma^2] variance of Gaussian noise;  
% >> params.alpha = 5; params.sigma = 0;
% 
% % Obtain the deconvolved image
% >> output = MW_PURE_LET(params);
% 
% % visualize the measurement and deconvolved image
% >> imshow([input output],[0 255])
% -------------------------------------------------------------------------
% 
% Authors: Jizhou Li, Florian Luisier and Thierry Blu
% References:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%             IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%               2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%               2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% Contact: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%   
% Last updated: 23 Aug, 2018

if(~isfield(params, 'oracle'))
    params.oracle = 0;
end

if(~isfield(params, 'input'))
    error('Please provide the input image');
end

if(~isfield(params, 'alpha'))
    params.alpha = aux_estAlpha(params.input);
end

if(~isfield(params, 'sigma'))
    params.sigma = sqrt(estimation_noise_variance(params.input));
end
    
input  = params.input;
sigma2 = params.sigma^2;
wtype  = 'haar';
alpha  = params.alpha;
H      = params.H;
S      = params.S;
mu     = 5e-2;
Ht     = conj(H);
H2     = Ht.*H;
S2     = conj(S).*S;
Y      = fft2(input);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Ey     = mean2(input);
% mu = alpha*Ey*1e-4;
En2    = alpha*Ey+sigma2;
beta   = 1e-5*En2;
lambda = logspace(-4,-2,3)*En2;
Hbi    = Ht./(H2+beta*S2);
Hbit   = conj(Hbi);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[nx,ny] = size(input); 
J = 4;
O = 3;
N = nx*ny;
B = J*O;
L = numel(lambda);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
Dx = zeros(nx,1,J,O);
Dy = zeros(1,ny,J,O);
for j = 1:J
    for o = 1:O
        [Dx(:,:,j,o),Dy(:,:,j,o)] = ...
            fft_wfilters1D(nx,ny,wtype,o-1,j);
    end
end
[Dlx,Dly] = fft_wfilters1D(nx,ny,wtype,3,J);
Dl = Dlx*Dly;
Rl = conj(Dl)/4^J;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
K   = 2;
F   = zeros(N,K*B*L);
div = zeros(K*B*L,1);
k   = 0;
Yl  = 0;
for l = 1:L
    Hli = Ht./(H2+lambda(l)*S2);
    Yi  = Hli.*Y;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Reconstruction of the Lowpass Residual:
    %----------------------------------------
    Fk = Rl.*Dl.*Yi;
    Yl = Yl+Fk/L;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Reconstruction of each Processed Highpass Subband:
    %---------------------------------------------------
    for j = 1:J
        for o = 1:O
            D  = Dx(:,:,j,o)*Dy(:,:,j,o);
            Di = D.*Hli;
            Dt = conj(D);
            R  = Dt/4^j;
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            di   = real(ifft2(Di));
            di2  = di.*di;
            Di2  = fft2(di2);
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            w      = real(ifft2(D.*Yi));
            v      = alpha*real(ifft2(Di2.*Y));
            
            sig2   = sigma2*mean2(abs(Di).^2);
            Thresh = aux_thresh(v,sig2,1);
            OK = mean2(w.*w)>2*mean2(Thresh.T.^2);
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            if(OK||params.oracle)
                %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                rt    = real(ifft2(conj(Hbit.*R)));
                DiRt  = fft2(di.*rt);
                Di2Rt = fft2(di2.*rt);
                w1    = real(ifft2(DiRt.*Y));
                w2    = real(ifft2(Di2Rt.*Y));
                %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                diagDR  = real(mean2(Di.*Hbit.*R));
                %diagD2R = real(mean2(Di2.*Hbit.*R));
                Di3     = fft2(di.*di2);
                diagD3R = real(mean2(Di3.*Hbit.*R));
                %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                Thresh.factor = 4;
                Theta  = aux_hard4(w,Thresh);
                k      = k+1;
                Fk     = R.*fft2(Theta.theta);
                F(:,k) = Fk(:);
                div(k) = alpha*Theta.theta1(:)'*w1(:);
                div(k) = div(k)+alpha*Theta.theta2(:)'*w2(:);
                div(k) = div(k)+sigma2*diagDR*sum(Theta.theta1(:));
                %div(k) = div(k)+sigma2*alpha*diagD2R*sum(Theta.theta2(:));
                div(k) = div(k)-2*alpha*sigma2*diagD3R*sum(Theta.theta12(:));
                %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                Thresh.factor = 9;
                Theta  = aux_hard4(w,Thresh);
                k      = k+1;
                Fk     = R.*fft2(Theta.theta);
                F(:,k) = Fk(:);
                div(k) = alpha*Theta.theta1(:)'*w1(:);
                div(k) = div(k)+alpha*Theta.theta2(:)'*w2(:);
                div(k) = div(k)+sigma2*diagDR*sum(Theta.theta1(:));
                %div(k) = div(k)+sigma2*alpha*diagD2R*sum(Theta.theta2(:));
                div(k) = div(k)-2*alpha*sigma2*diagD3R*sum(Theta.theta12(:));
                %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            end
        end
    end
end
u   = div~=0;
F   = F(:,u);
div = div(u);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
A  = real(F'*F)/N^2;
if(params.oracle)
    % Global Image-domain MSE Minimization:
    %--------------------------------------
    X = fft2(params.original);
    c = real(F'*(X(:)-Yl(:)))/N^2;
    a = pinv(A)*c;
else
    % Global Image-domain PURE Minimization:
    %---------------------------------------
    Ybi = Hbi.*Y;
    c = real(F'*(Ybi(:)-Yl(:)))/N^2-div/N;
    c = max(c,0);
    I = eye(k);
    a = (A+mu*I)\c;
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
output = abs(ifft2(reshape(F*a,nx,ny)+Yl));

